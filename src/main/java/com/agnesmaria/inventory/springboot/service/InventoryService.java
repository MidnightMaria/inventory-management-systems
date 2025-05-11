package com.agnesmaria.inventory.springboot.service;

import com.agnesmaria.inventory.springboot.dto.InventoryRequest;
import com.agnesmaria.inventory.springboot.model.InventoryItem;
import com.agnesmaria.inventory.springboot.model.Product;
import com.agnesmaria.inventory.springboot.model.Warehouse;
import com.agnesmaria.inventory.springboot.repository.InventoryItemRepository;
import com.agnesmaria.inventory.springboot.repository.WarehouseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final ProductService productService;
    private final WarehouseRepository warehouseRepository; // Add this
    private final InventoryItemRepository inventoryItemRepository;

    @Transactional
    public void updateStock(InventoryRequest request) {
        Product product = productService.getProductBySku(request.getProductSku());
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
            .orElseThrow(() -> new RuntimeException("Warehouse not found"));
        
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