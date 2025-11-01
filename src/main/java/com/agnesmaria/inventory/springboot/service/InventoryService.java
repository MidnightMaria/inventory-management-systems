package com.agnesmaria.inventory.springboot.service;

import com.agnesmaria.inventory.springboot.dto.*;
import com.agnesmaria.inventory.springboot.exception.InventoryQueryException;
import com.agnesmaria.inventory.springboot.exception.InventoryUpdateException;
import com.agnesmaria.inventory.springboot.exception.WarehouseNotFoundException;
import com.agnesmaria.inventory.springboot.model.*;
import com.agnesmaria.inventory.springboot.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    /* =========================================================
     * Helpers
     * ========================================================= */

    private InventoryItem getOrCreateItem(Product product, Warehouse warehouse) {
        return inventoryItemRepository
                .findByProductAndWarehouse(product, warehouse)
                .orElseGet(() -> InventoryItem.builder()
                        .product(product)
                        .warehouse(warehouse)
                        .quantity(0)
                        .build());
    }

    private void syncProductStock(Product product) {
        Integer totalStock = inventoryItemRepository.sumQuantityByProductSku(product.getSku());
        product.setQuantity(totalStock != null ? totalStock : 0);
        productRepository.save(product);
    }

    private void recordMovement(Product product, Warehouse warehouse, Integer oldQty, Integer newQty,
                                String movementType, String reason, String referenceNumber, String performedBy) {

        InventoryMovement movement = InventoryMovement.builder()
                .product(product)
                .warehouse(warehouse)
                .previousQuantity(oldQty != null ? oldQty : 0)
                .newQuantity(newQty != null ? newQty : 0)
                .difference((newQty != null ? newQty : 0) - (oldQty != null ? oldQty : 0))
                .movementType(movementType)
                .reason(Optional.ofNullable(reason).orElse("N/A"))
                .referenceNumber(Optional.ofNullable(referenceNumber).orElse("N/A"))
                .performedBy(Optional.ofNullable(performedBy).orElse("SYSTEM"))
                .build();

        inventoryMovementRepository.save(movement);
        log.info("📦 Movement recorded [{}] SKU={} WH={} ({}→{}) Ref={}",
                movementType, product.getSku(), warehouse.getCode(), oldQty, newQty, referenceNumber);
    }

    private InventoryResponse buildResponse(Product product, Warehouse warehouse, int oldQty, int newQty, String movementType) {
        return InventoryResponse.builder()
                .productSku(product.getSku())
                .productName(product.getName())
                .warehouseCode(warehouse.getCode())
                .previousStock(oldQty)
                .currentStock(newQty)
                .difference(newQty - oldQty)
                .movementType(movementType)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /* =========================================================
     * Stock Adjustments
     * ========================================================= */

    @Transactional
    public InventoryResponse updateStock(InventoryRequest request) {
        if (request.getQuantity() < 0) {
            throw new InventoryUpdateException("Quantity must be >= 0");
        }

        String movementType = Optional.ofNullable(request.getMovementType())
                .map(s -> s.toUpperCase(Locale.ROOT))
                .orElse("ADJUST");

        Product product = productService.getProductBySku(request.getProductSku());
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new WarehouseNotFoundException(request.getWarehouseId()));

        InventoryItem item = getOrCreateItem(product, warehouse);

        int oldQty = item.getQuantity();
        int newQty = oldQty;

        switch (movementType) {
            case "IN" -> newQty = oldQty + request.getQuantity();
            case "OUT" -> {
                if (oldQty < request.getQuantity()) {
                    throw new InventoryUpdateException("Insufficient stock in warehouse");
                }
                newQty = oldQty - request.getQuantity();
            }
            case "ADJUST", "ADJUSTMENT" -> newQty = request.getQuantity();
            default -> log.warn("Unknown movementType '{}', no changes applied.", movementType);
        }

        item.setQuantity(newQty);
        inventoryItemRepository.save(item);

        recordMovement(product, warehouse, oldQty, newQty,
                movementType, request.getAdjustmentReason(), request.getReferenceNumber(), "SYSTEM");

        syncProductStock(product);

        log.info("✅ Updated stock SKU={} in WH={} ({}→{}) [{}]",
                product.getSku(), warehouse.getCode(), oldQty, newQty, movementType);

        return buildResponse(product, warehouse, oldQty, newQty, movementType);
    }

    /* =========================================================
     * Internal and Sales Stock Reductions
     * ========================================================= */

    @Transactional
    public InventoryResponse reduceStock(InventoryRequest request) {
        request.setMovementType("OUT");
        return updateStock(request);
    }

    @Transactional
    public InventoryResponse updateStockInternal(InventoryRequest request) {
        log.info("🔗 Internal stock update triggered for SKU: {}", request.getProductSku());
        return updateStock(request);
    }

    /* =========================================================
     * Transfer Between Warehouses
     * ========================================================= */

    @Transactional
    public StockTransferResponse transferStock(StockTransferRequest request) {
        Long fromId = request.getFromWarehouseId();
        Long toId = request.getToWarehouseId();
        String sku = request.getProductSku();
        int qty = request.getQuantity();

        if (qty <= 0) {
            throw new InventoryUpdateException("Transfer quantity must be > 0");
        }
        if (fromId.equals(toId)) {
            throw new InventoryUpdateException("Source and destination warehouse cannot be the same");
        }

        log.info("🚚 Transfer {} units of SKU {} from WH:{} → WH:{} (ref={})",
                qty, sku, fromId, toId, request.getReference());

        Warehouse fromWarehouse = warehouseRepository.findById(fromId)
                .orElseThrow(() -> new WarehouseNotFoundException(fromId));
        Warehouse toWarehouse = warehouseRepository.findById(toId)
                .orElseThrow(() -> new WarehouseNotFoundException(toId));

        Product product = productService.getProductBySku(sku);

        InventoryItem fromItem = inventoryItemRepository.findByWarehouseAndProductSku(fromWarehouse, sku)
                .orElseGet(() -> InventoryItem.builder()
                        .warehouse(fromWarehouse)
                        .product(product)
                        .quantity(0)
                        .build());

        InventoryItem toItem = inventoryItemRepository.findByWarehouseAndProductSku(toWarehouse, sku)
                .orElseGet(() -> InventoryItem.builder()
                        .warehouse(toWarehouse)
                        .product(product)
                        .quantity(0)
                        .build());

        int oldFrom = fromItem.getQuantity();
        int oldTo = toItem.getQuantity();

        if (oldFrom < qty) {
            throw new InventoryUpdateException("Insufficient stock in source warehouse");
        }

        fromItem.setQuantity(oldFrom - qty);
        toItem.setQuantity(oldTo + qty);

        inventoryItemRepository.save(fromItem);
        inventoryItemRepository.save(toItem);

        recordMovement(product, fromWarehouse, oldFrom, fromItem.getQuantity(),
                "TRANSFER_OUT", "Transfer to " + toWarehouse.getCode(), request.getReference(), "SYSTEM");

        recordMovement(product, toWarehouse, oldTo, toItem.getQuantity(),
                "TRANSFER_IN", "Transfer from " + fromWarehouse.getCode(), request.getReference(), "SYSTEM");

        syncProductStock(product);

        log.info("✅ Transfer done: SKU={} {} -> {} | {}→{} & {}→{}",
                sku, fromWarehouse.getCode(), toWarehouse.getCode(),
                oldFrom, fromItem.getQuantity(), oldTo, toItem.getQuantity());

        return StockTransferResponse.builder()
                .status("SUCCESS")
                .message("Stock transferred successfully")
                .productSku(sku)
                .quantity(qty)
                .fromWarehouseId(fromId)
                .toWarehouseId(toId)
                .build();
    }

    /* =========================================================
     * Queries / Exports
     * ========================================================= */

    @Transactional(readOnly = true)
    public Integer getTotalStockByProduct(String sku) {
        Integer total = inventoryItemRepository.sumQuantityByProductSku(sku);
        return total != null ? total : 0;
    }

    @Transactional(readOnly = true)
    public Integer getStockByProductAndWarehouse(String sku, Long warehouseId) {
        return inventoryItemRepository.findQuantityByProductAndWarehouse(sku, warehouseId).orElse(0);
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventory() {
        return inventoryItemRepository.findAll().stream()
                .map(item -> InventoryResponse.builder()
                        .productSku(item.getProduct().getSku())
                        .productName(item.getProduct().getName())
                        .warehouseCode(item.getWarehouse().getCode())
                        .currentStock(item.getQuantity())
                        .updatedAt(item.getUpdatedAt())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementDTO> getAllMovements() {
        return inventoryMovementRepository.findAll().stream()
                .map(m -> InventoryMovementDTO.builder()
                        .id(m.getId())
                        .productSku(m.getProduct().getSku())
                        .productName(m.getProduct().getName())
                        .warehouseCode(
                                m.getWarehouse() != null ? m.getWarehouse().getCode() :
                                        (m.getToWarehouse() != null ? m.getToWarehouse().getCode() : "-")
                        )
                        .movementType(m.getMovementType())
                        .difference(m.getDifference())
                        .reason(m.getReason())
                        .referenceNumber(m.getReferenceNumber())
                        .performedBy(m.getPerformedBy())
                        .createdAt(m.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> exportMovementSummary() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        return inventoryMovementRepository.findAll().stream()
                .filter(m -> Optional.ofNullable(m.getCreatedAt()).orElse(LocalDateTime.MIN).isAfter(sixMonthsAgo))
                .map(m -> InventoryMovementResponse.builder()
                        .productSku(m.getProduct().getSku())
                        .warehouseCode(m.getWarehouse().getCode())
                        .movementType(m.getMovementType())
                        .difference(m.getDifference())
                        .reason(m.getReason())
                        .referenceNumber(m.getReferenceNumber())
                        .createdAt(m.getCreatedAt())
                        .build())
                .toList();
    }
}
