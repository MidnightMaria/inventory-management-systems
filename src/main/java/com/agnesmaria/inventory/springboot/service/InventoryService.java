package com.agnesmaria.inventory.springboot.service;

import com.agnesmaria.inventory.springboot.dto.InventoryRequest;
import com.agnesmaria.inventory.springboot.dto.InventoryResponse;
import com.agnesmaria.inventory.springboot.exception.*;
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

    @Transactional
    public InventoryResponse updateStock(InventoryRequest request) {
        try {
            Product product = productService.getProductBySku(request.getProductSku());
            Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new WarehouseNotFoundException(request.getWarehouseId()));

            InventoryItem item = inventoryItemRepository
                    .findByProductAndWarehouse(product, warehouse)
                    .orElseGet(() -> InventoryItem.builder()
                            .product(product)
                            .warehouse(warehouse)
                            .quantity(0)
                            .build());

            int oldQuantity = item.getQuantity();
            int newQuantity = switch (request.getMovementType()) {
                case "IN" -> oldQuantity + request.getQuantity();
                case "OUT" -> Math.max(0, oldQuantity - request.getQuantity());
                case "ADJUST" -> request.getQuantity();
                default -> oldQuantity;
            };

            item.setQuantity(newQuantity);
            inventoryItemRepository.save(item);

            recordInventoryMovement(product, warehouse, oldQuantity, newQuantity,
                    request.getMovementType(), request.getAdjustmentReason(), request.getReferenceNumber());

            syncProductStock(product);
            return buildResponse(product, warehouse, item, oldQuantity);

        } catch (Exception e) {
            log.error("💥 Error updating stock", e);
            throw new InventoryUpdateException("Failed to update inventory stock", e);
        }
    }

    @Transactional
    public InventoryResponse updateStockInternal(InventoryRequest request) {
        log.info("🔗 Internal stock update triggered for {}", request.getProductSku());
        return updateStock(request);
    }

    private void recordInventoryMovement(Product product, Warehouse warehouse,
                                         int oldQty, int newQty, String movementType,
                                         String reason, String reference) {
        try {
            InventoryMovement movement = InventoryMovement.builder()
                    .product(product)
                    .warehouse(warehouse)
                    .previousQuantity(oldQty)
                    .newQuantity(newQty)
                    .difference(newQty - oldQty)
                    .movementType(movementType)
                    .reason(reason)
                    .referenceNumber(reference)
                    .build();
            inventoryMovementRepository.save(movement);
        } catch (Exception e) {
            log.warn("⚠️ Failed to record movement: {}", e.getMessage());
        }
    }

    private void syncProductStock(Product product) {
        Integer totalStock = inventoryItemRepository.sumQuantityByProductSku(product.getSku());
        product.setQuantity(totalStock != null ? totalStock : 0);
        productRepository.save(product);
    }

    private InventoryResponse buildResponse(Product product, Warehouse warehouse,
                                            InventoryItem item, int oldQuantity) {
        return InventoryResponse.builder()
                .productSku(product.getSku())
                .productName(product.getName())
                .warehouseCode(warehouse.getCode())
                .previousStock(oldQuantity)
                .currentStock(item.getQuantity())
                .difference(item.getQuantity() - oldQuantity)
                .movementType(item.getQuantity() > oldQuantity ? "IN" : "OUT")
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public Integer getTotalStockByProduct(String sku) {
        return inventoryItemRepository.sumQuantityByProductSku(sku);
    }

    public Integer getStockByProductAndWarehouse(String sku, Long warehouseId) {
        return inventoryItemRepository.findQuantityByProductAndWarehouse(sku, warehouseId)
                .orElse(0);
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

    public InventoryResponse reduceStock(InventoryRequest request) {
        Product product = productService.getProductBySku(request.getProductSku());
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new WarehouseNotFoundException(request.getWarehouseId()));

        InventoryItem item = inventoryItemRepository.findByProductAndWarehouse(product, warehouse)
                .orElseThrow(() -> new InventoryQueryException("No stock record found"));

        int oldQuantity = item.getQuantity();
        int newQuantity = Math.max(0, oldQuantity - request.getQuantity());
        item.setQuantity(newQuantity);
        inventoryItemRepository.save(item);

        recordInventoryMovement(product, warehouse, oldQuantity, newQuantity,
                "OUT", request.getAdjustmentReason(), request.getReferenceNumber());
        syncProductStock(product);

        return buildResponse(product, warehouse, item, oldQuantity);
    }
}
