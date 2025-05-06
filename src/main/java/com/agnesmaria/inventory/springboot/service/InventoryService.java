package com.agnesmaria.inventory.springboot.service;

import com.agnesmaria.inventory.springboot.dto.InventoryRequest;
import com.agnesmaria.inventory.springboot.model.InventoryItem;
import com.agnesmaria.inventory.springboot.model.Product;
import com.agnesmaria.inventory.springboot.model.Warehouse;
import com.agnesmaria.inventory.springboot.repository.InventoryItemRepository;
import jakarta.transaction.Transactional; // Atau org.springframework.transaction.annotation.Transactional
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final ProductService productService;
    private final WarehouseService warehouseService;
    private final InventoryItemRepository inventoryItemRepository;

    @Transactional
    public void updateStock(InventoryRequest request) {
        Product product = productService.getProductBySku(request.getProductSku());
        Warehouse warehouse = warehouseService.getWarehouseById(request.getWarehouseId());
        
        InventoryItem item = inventoryItemRepository
            .findByProductAndWarehouse(product, warehouse)
            .orElseGet(() -> new InventoryItem(product, warehouse, 0));
        
        item.setQuantity(request.getQuantity());
        inventoryItemRepository.save(item);
    }
    
    @Transactional
    public int getTotalStockByProduct(String sku) {
        return inventoryItemRepository.sumQuantityByProductSku(sku);
    }
}