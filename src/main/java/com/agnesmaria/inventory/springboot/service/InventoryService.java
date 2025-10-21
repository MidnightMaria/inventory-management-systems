package com.agnesmaria.inventory.springboot.service;

import com.agnesmaria.inventory.springboot.dto.InventoryRequest;
import com.agnesmaria.inventory.springboot.dto.InventoryResponse;
import com.agnesmaria.inventory.springboot.exception.*;
import com.agnesmaria.inventory.springboot.model.*;
import com.agnesmaria.inventory.springboot.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final UserRepository userRepository;

    // 🔹 Menyesuaikan stok (bisa IN / OUT / ADJUST)
    @Transactional
    public InventoryResponse updateStock(InventoryRequest request) {
        try {
            Product product = productService.getProductBySku(request.getProductSku());
            if (product == null) throw new ProductNotFoundException(request.getProductSku());

            Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                    .orElseThrow(() -> new WarehouseNotFoundException(request.getWarehouseId()));

            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException(username));

            InventoryItem item = inventoryItemRepository
                    .findByProductAndWarehouse(product, warehouse)
                    .orElseGet(() -> {
                        log.info("🆕 Creating new inventory item for product {} in warehouse {}",
                                product.getSku(), warehouse.getCode());
                        return InventoryItem.builder()
                                .product(product)
                                .warehouse(warehouse)
                                .quantity(0)
                                .build();
                    });

            int oldQuantity = item.getQuantity();
            int newQuantity = oldQuantity;

            switch (request.getMovementType()) {
                case "IN" -> newQuantity = oldQuantity + request.getQuantity();
                case "OUT" -> {
                    if (oldQuantity < request.getQuantity())
                        throw new InventoryUpdateException("Insufficient stock to reduce");
                    newQuantity = oldQuantity - request.getQuantity();
                }
                case "ADJUST" -> newQuantity = request.getQuantity();
                default -> throw new InventoryUpdateException("Invalid movement type");
            }

            item.setQuantity(newQuantity);
            InventoryItem savedItem = inventoryItemRepository.save(item);

            // 📦 Catat pergerakan stok
            recordInventoryMovement(product, warehouse, user, oldQuantity, newQuantity,
                    request.getMovementType(), request.getAdjustmentReason(), request.getReferenceNumber());

            // 🔄 Sinkronisasi total stok ke Product
            syncProductStock(product);

            log.info("✅ Stock updated for {} in {} ({} → {}) by {}",
                    product.getSku(), warehouse.getCode(), oldQuantity, newQuantity, user.getUsername());

            return buildInventoryResponse(product, warehouse, savedItem, oldQuantity);

        } catch (ProductNotFoundException | WarehouseNotFoundException | UserNotFoundException e) {
            log.error("❌ Validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("💥 Unexpected error during stock update", e);
            throw new InventoryUpdateException("Failed to update inventory stock", e);
        }
    }

    // 🔹 Mengurangi stok (misal karena penjualan)
    @Transactional
    public InventoryResponse reduceStock(InventoryRequest request) {
        Product product = productService.getProductBySku(request.getProductSku());
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new WarehouseNotFoundException(request.getWarehouseId()));

        InventoryItem item = inventoryItemRepository.findByProductAndWarehouse(product, warehouse)
                .orElseThrow(() -> new InventoryQueryException("No stock record found"));

        int oldQuantity = item.getQuantity();
        int reduceBy = request.getQuantity();

        if (reduceBy > oldQuantity)
            throw new InventoryUpdateException("Insufficient stock to reduce");

        // 🔻 Update stok di warehouse
        item.setQuantity(oldQuantity - reduceBy);
        inventoryItemRepository.save(item);

        // 🔄 Sinkronisasi ke tabel Product
        syncProductStock(product);

        // 🧾 Catat movement stok
        User user = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName())
                .orElse(null);

        recordInventoryMovement(product, warehouse, user, oldQuantity, item.getQuantity(),
                "OUT", request.getAdjustmentReason(), request.getReferenceNumber());

        log.info("📦 Stock reduced for {}: {} → {} in {}",
                product.getSku(), oldQuantity, item.getQuantity(), warehouse.getCode());

        return InventoryResponse.builder()
                .productSku(product.getSku())
                .productName(product.getName())
                .warehouseCode(warehouse.getCode())
                .previousStock(oldQuantity)
                .currentStock(item.getQuantity())
                .difference(-reduceBy)
                .movementType("OUT")
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // 🔹 Mencatat pergerakan stok
    private void recordInventoryMovement(
            Product product, Warehouse warehouse, User user,
            int oldQty, int newQty, String movementType, String reason, String reference
    ) {
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
                    .performedBy(user)
                    .build();
            inventoryMovementRepository.save(movement);
            log.info("📝 Movement recorded: {} {} ({} → {})", movementType, product.getSku(), oldQty, newQty);
        } catch (Exception e) {
            log.error("⚠️ Failed to record movement for {}: {}", product.getSku(), e.getMessage());
        }
    }

    // 🔹 Sinkronisasi total stok dari semua warehouse ke tabel Product
    private void syncProductStock(Product product) {
        try {
            Integer totalStock = inventoryItemRepository.sumQuantityByProductSku(product.getSku());
            product.setQuantity(totalStock != null ? totalStock : 0);
            productRepository.save(product);
            log.info("🔁 Synced product {} total quantity to {}", product.getSku(), product.getQuantity());
        } catch (Exception e) {
            log.warn("⚠️ Failed to sync stock for {}: {}", product.getSku(), e.getMessage());
        }
    }

    // 🔹 Response builder
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

    // 🔹 Total stok semua warehouse
    @Transactional(readOnly = true)
    public Integer getTotalStockByProduct(String sku) {
        try {
            return inventoryItemRepository.sumQuantityByProductSku(sku);
        } catch (Exception e) {
            log.error("Error calculating total stock for product {}", sku, e);
            throw new InventoryQueryException("Failed to calculate total stock", e);
        }
    }

    // 🔹 Stok per warehouse
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

    // 🔹 Export semua data inventory
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
