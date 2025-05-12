package com.agnesmaria.inventory.springboot.service;

import com.agnesmaria.inventory.springboot.dto.InventoryRequest;
import com.agnesmaria.inventory.springboot.dto.InventoryResponse;
import com.agnesmaria.inventory.springboot.exception.*;
import com.agnesmaria.inventory.springboot.model.*;
import com.agnesmaria.inventory.springboot.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {
    private final ProductService productService;
    private final WarehouseRepository warehouseRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final UserRepository userRepository;

    @Transactional
    public InventoryResponse updateStock(InventoryRequest request) {
        try {
            // Validate product
            Product product = productService.getProductBySku(request.getProductSku());
            if (product == null) {
                throw new ProductNotFoundException(request.getProductSku());
            }

            // Validate warehouse
            Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new WarehouseNotFoundException(request.getWarehouseId()));

            // Get current user
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

            // Find or create inventory item
            InventoryItem item = inventoryItemRepository
                .findByProductAndWarehouse(product, warehouse)
                .orElseGet(() -> {
                    log.info("Creating new inventory item for product {} in warehouse {}", 
                            product.getSku(), warehouse.getCode());
                    return InventoryItem.builder()
                            .product(product)
                            .warehouse(warehouse)
                            .quantity(0)
                            .build();
                });

            int oldQuantity = item.getQuantity();
            int newQuantity = request.getQuantity();
            item.setQuantity(newQuantity);
            InventoryItem savedItem = inventoryItemRepository.save(item);

            // Record movement
            InventoryMovement movement = recordInventoryMovement(
                    product, warehouse, user,
                    oldQuantity, newQuantity,
                    request.getMovementType(),
                    request.getAdjustmentReason(),
                    request.getReferenceNumber());

            log.info("Stock updated: {} in {} ({} → {}). Movement ID: {}", 
                    product.getSku(), warehouse.getCode(), 
                    oldQuantity, newQuantity, movement.getId());

            return buildInventoryResponse(product, warehouse, savedItem, oldQuantity);

        } catch (ProductNotFoundException | WarehouseNotFoundException | UserNotFoundException e) {
            log.error("Validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during stock update", e);
            throw new InventoryUpdateException("Failed to update inventory stock", e);
        }
    }

    private InventoryMovement recordInventoryMovement(Product product, Warehouse warehouse, User user,
                                                    int oldQty, int newQty, 
                                                    String movementType, String reason, String reference) {
        InventoryMovement movement = InventoryMovement.builder()
                .product(product)
                .warehouse(warehouse)
                .previousQuantity(oldQty)
                .newQuantity(newQty)
                .difference(newQty - oldQty)
                .movementType(movementType)
                .reason(reason)
                .referenceNumber(reference)
                .performedBy(user)
                .build();
        return inventoryMovementRepository.save(movement);
    }

    private InventoryResponse buildInventoryResponse(Product product, Warehouse warehouse, 
                                                   InventoryItem item, int oldQuantity) {
        return InventoryResponse.builder()
                .productSku(product.getSku())
                .productName(product.getName())
                .warehouseCode(warehouse.getCode())
                .previousStock(oldQuantity)
                .currentStock(item.getQuantity())
                .difference(item.getQuantity() - oldQuantity)
                .movementType(item.getQuantity() > oldQuantity ? "IN" : "OUT")
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public Integer getTotalStockByProduct(String sku) {
        try {
            return inventoryItemRepository.sumQuantityByProductSku(sku);
        } catch (Exception e) {
            log.error("Error calculating total stock for product {}", sku, e);
            throw new InventoryQueryException("Failed to calculate total stock", e);
        }
    }

    @Transactional(readOnly = true)
    public Integer getStockByProductAndWarehouse(String sku, Long warehouseId) {
        try {
            return inventoryItemRepository.findQuantityByProductAndWarehouse(sku, warehouseId)
                    .orElse(0);
        } catch (Exception e) {
            log.error("Error getting stock for product {} in warehouse {}", sku, warehouseId, e);
            throw new InventoryQueryException("Failed to get warehouse stock", e);
        }
    }
}