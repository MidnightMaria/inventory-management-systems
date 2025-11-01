package com.agnesmaria.inventory.springboot.service;

import com.agnesmaria.inventory.springboot.dto.InventoryMovementResponse;
import com.agnesmaria.inventory.springboot.dto.InventoryRequest;
import com.agnesmaria.inventory.springboot.dto.InventoryResponse;
import com.agnesmaria.inventory.springboot.dto.StockTransferRequest;
import com.agnesmaria.inventory.springboot.dto.StockTransferResponse;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductService productService;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    // ✅ Manual or internal stock adjustment
    @Transactional
    public InventoryResponse updateStock(InventoryRequest request) {
        Product product = productService.getProductBySku(request.getProductSku());
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new WarehouseNotFoundException(request.getWarehouseId()));

        InventoryItem item = inventoryItemRepository.findByProductAndWarehouse(product, warehouse)
                .orElseGet(() -> InventoryItem.builder()
                        .product(product)
                        .warehouse(warehouse)
                        .quantity(0)
                        .build());

        int oldQty = item.getQuantity();
        int newQty = switch (request.getMovementType()) {
            case "IN" -> oldQty + request.getQuantity();
            case "OUT" -> Math.max(0, oldQty - request.getQuantity());
            case "ADJUST", "ADJUSTMENT" -> request.getQuantity();
            default -> oldQty;
        };

        item.setQuantity(newQty);
        inventoryItemRepository.save(item);

        recordMovement(product, warehouse, oldQty, newQty,
                request.getMovementType(), request.getAdjustmentReason(),
                request.getReferenceNumber(), "SYSTEM");

        syncProductStock(product);

        log.info("✅ Updated stock for {} ({} → {}) in warehouse {}", product.getSku(), oldQty, newQty, warehouse.getCode());
        return buildResponse(product, warehouse, item, oldQty);
    }

    @Transactional
    public StockTransferResponse transferStock(StockTransferRequest request) {
        Long fromId = request.getFromWarehouseId();
        Long toId = request.getToWarehouseId();
        String sku = request.getProductSku();
        int qty = request.getQuantity();

        // 🔍 Ambil data warehouse & item asal
        Warehouse fromWarehouse = warehouseRepository.findById(fromId)
                .orElseThrow(() -> new WarehouseNotFoundException("Source warehouse not found"));
        Warehouse toWarehouse = warehouseRepository.findById(toId)
                .orElseThrow(() -> new WarehouseNotFoundException("Destination warehouse not found"));

        InventoryItem fromItem = inventoryItemRepository.findByWarehouseAndProductSku(fromWarehouse, sku)
                .orElseThrow(() -> new InventoryQueryException("Product not found in source warehouse"));

        if (fromItem.getQuantity() < qty) {
            throw new InventoryUpdateException("Insufficient stock in source warehouse");
        }

        // 🔽 Kurangi stok di gudang asal
        fromItem.setQuantity(fromItem.getQuantity() - qty);
        inventoryItemRepository.save(fromItem);

        // 🔼 Tambah stok di gudang tujuan
        InventoryItem toItem = inventoryItemRepository.findByWarehouseAndProductSku(toWarehouse, sku)
                .orElseGet(() -> {
                    InventoryItem newItem = new InventoryItem();
                    newItem.setWarehouse(toWarehouse);
                    newItem.setProduct(fromItem.getProduct());
                    newItem.setQuantity(0);
                    return newItem;
                });
        toItem.setQuantity(toItem.getQuantity() + qty);
        inventoryItemRepository.save(toItem);

        // 🧾 Catat movement log
        InventoryMovement movement = new InventoryMovement();
        movement.setProduct(fromItem.getProduct());
        movement.setQuantity(qty);
        movement.setMovementType("TRANSFER");
        movement.setFromWarehouse(fromWarehouse);
        movement.setToWarehouse(toWarehouse);
        movement.setReferenceNumber(request.getReference());
        inventoryMovementRepository.save(movement);

        log.info("✅ Transferred {} of {} from {} to {}",
                qty, sku, fromWarehouse.getCode(), toWarehouse.getCode());

        return StockTransferResponse.builder()
                .status("SUCCESS")
                .message("Stock transferred successfully")
                .productSku(sku)
                .quantity(qty)
                .fromWarehouseId(fromId)
                .toWarehouseId(toId)
                .build();
    }


    // 🔗 Used by Supply Chain Service integration
    @Transactional
    public InventoryResponse updateStockInternal(InventoryRequest request) {
        log.info("🔗 Internal stock update triggered from Supply Chain for {}", request.getProductSku());
        return updateStock(request);
    }

    // 🚚 Reduce stock due to sales/shipment
    @Transactional
    public InventoryResponse reduceStock(InventoryRequest request) {
        request.setMovementType("OUT");
        return updateStock(request);
    }

    // 🧾 Record stock movement (for audit + analytics)
    private void recordMovement(Product product, Warehouse warehouse, int oldQty, int newQty,
                                String movementType, String reason, String referenceNumber, String performedBy) {

        InventoryMovement movement = InventoryMovement.builder()
                .product(product)
                .warehouse(warehouse)
                .previousQuantity(oldQty)
                .newQuantity(newQty)
                .difference(newQty - oldQty)
                .movementType(movementType)
                .reason(reason != null ? reason : "N/A")
                .referenceNumber(referenceNumber != null ? referenceNumber : "N/A")
                .performedBy(performedBy != null ? performedBy : "SYSTEM")
                .build();

        inventoryMovementRepository.save(movement);
        log.info("📦 Movement recorded: [{}] {} {} ({} → {}) - Ref: {}", movementType, product.getSku(), reason, oldQty, newQty, referenceNumber);
    }

    // 🔁 Update total stock on product table
    private void syncProductStock(Product product) {
        Integer totalStock = inventoryItemRepository.sumQuantityByProductSku(product.getSku());
        product.setQuantity(totalStock != null ? totalStock : 0);
        productRepository.save(product);
    }

    // 📄 Build response for API
    private InventoryResponse buildResponse(Product product, Warehouse warehouse,
                                            InventoryItem item, int oldQty) {
        return InventoryResponse.builder()
                .productSku(product.getSku())
                .productName(product.getName())
                .warehouseCode(warehouse.getCode())
                .previousStock(oldQty)
                .currentStock(item.getQuantity())
                .difference(item.getQuantity() - oldQty)
                .movementType(item.getQuantity() > oldQty ? "IN" : "OUT")
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // 🔍 Query utilities
    public Integer getTotalStockByProduct(String sku) {
        return inventoryItemRepository.sumQuantityByProductSku(sku);
    }

    public Integer getStockByProductAndWarehouse(String sku, Long warehouseId) {
        return inventoryItemRepository.findQuantityByProductAndWarehouse(sku, warehouseId).orElse(0);
    }

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

    // 📜 Get all movements (for monitoring)
    public List<InventoryMovement> getAllMovements() {
        return inventoryMovementRepository.findAll();
    }

    // 📤 Export data for analytics (6 months window)
    public List<InventoryMovement> exportMovements() {
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        return inventoryMovementRepository.findAll().stream()
                .filter(m -> m.getCreatedAt().isAfter(sixMonthsAgo))
                .filter(m -> m.getMovementType().equalsIgnoreCase("OUT")
                        || m.getMovementType().equalsIgnoreCase("IN"))
                .toList();
    }

    public List<InventoryMovementResponse> exportMovementSummary() {
    LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
    return inventoryMovementRepository.findAll().stream()
            .filter(m -> m.getCreatedAt().isAfter(sixMonthsAgo))
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