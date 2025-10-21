package com.agnesmaria.inventory.springboot.service;

import com.agnesmaria.inventory.springboot.dto.InventoryRequest;
import com.agnesmaria.inventory.springboot.dto.InventoryResponse;
import com.agnesmaria.inventory.springboot.exception.*;
import com.agnesmaria.inventory.springboot.model.*;
import com.agnesmaria.inventory.springboot.repository.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductService productService;
    private final ProductRepository productRepository; // ✅ tambahkan repository
    private final WarehouseRepository warehouseRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final UserRepository userRepository;

    @Transactional
    public InventoryResponse updateStock(InventoryRequest request) {
        try {
            // ✅ 1️⃣ Validasi produk
            Product product = productService.getProductBySku(request.getProductSku());
            if (product == null) {
                throw new ProductNotFoundException(request.getProductSku());
            }

            // ✅ 2️⃣ Validasi warehouse
            Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new WarehouseNotFoundException(request.getWarehouseId()));

            // ✅ 3️⃣ Dapatkan user yang sedang login
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException(username));

            // ✅ 4️⃣ Temukan atau buat item inventory
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

            // ✅ 5️⃣ Catat pergerakan stok
            InventoryMovement movement = recordInventoryMovement(
                    product, warehouse, user,
                    oldQuantity, newQuantity,
                    request.getMovementType(),
                    request.getAdjustmentReason(),
                    request.getReferenceNumber());

            // ✅ 6️⃣ Sinkronisasi stok total produk (update ke tabel Product)
            try {
                Integer totalStock = inventoryItemRepository.sumQuantityByProductSku(product.getSku());
                product.setQuantity(totalStock != null ? totalStock : 0);
                productRepository.save(product); // ✅ simpan langsung ke repository
                log.info("✅ Product {} quantity synchronized to {}", product.getSku(), product.getQuantity());
            } catch (Exception syncEx) {
                log.warn("⚠️ Failed to sync product stock for {}: {}", product.getSku(), syncEx.getMessage());
            }

            log.info("Stock updated: {} in {} ({} → {}). Movement ID: {}",
                    product.getSku(), warehouse.getCode(),
                    oldQuantity, newQuantity, movement.getId());

            return buildInventoryResponse(product, warehouse, savedItem, oldQuantity);

        } catch (ProductNotFoundException | WarehouseNotFoundException | UserNotFoundException e) {
            log.error("❌ Validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("💥 Unexpected error during stock update", e);
            throw new InventoryUpdateException("Failed to update inventory stock", e);
        }
    }

    // 📦 Mencatat pergerakan stok
    private InventoryMovement recordInventoryMovement(
            Product product, Warehouse warehouse, User user,
            int oldQty, int newQty,
            String movementType, String reason, String reference
    ) {
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

    // 🧾 Membentuk response untuk frontend
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

    // 📊 Total stok semua warehouse
    @Transactional(readOnly = true)
    public Integer getTotalStockByProduct(String sku) {
        try {
            return inventoryItemRepository.sumQuantityByProductSku(sku);
        } catch (Exception e) {
            log.error("Error calculating total stock for product {}", sku, e);
            throw new InventoryQueryException("Failed to calculate total stock", e);
        }
    }

    // 📍 Stok per warehouse
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

    // 📤 Export semua data inventory
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllInventory() {
        return inventoryItemRepository.findAll().stream()
                .map(item -> InventoryResponse.builder()
                        .productSku(item.getProduct().getSku())
                        .productName(item.getProduct().getName())
                        .warehouseCode(item.getWarehouse().getCode())
                        .currentStock(item.getQuantity())
                        .movementType("STATIC")
                        .updatedAt(item.getUpdatedAt())
                        .build())
                .toList();
    }
}
