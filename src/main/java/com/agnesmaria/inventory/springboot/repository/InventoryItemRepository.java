package com.agnesmaria.inventory.springboot.repository;

import com.agnesmaria.inventory.springboot.model.InventoryItem;
import com.agnesmaria.inventory.springboot.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByProductAndWarehouse(com.agnesmaria.inventory.springboot.model.Product product,
                                                      Warehouse warehouse);

    @Query("SELECT i FROM InventoryItem i WHERE i.warehouse = :warehouse AND i.product.sku = :sku")
    Optional<InventoryItem> findByWarehouseAndProductSku(Warehouse warehouse, String sku);

    @Query("SELECT SUM(i.quantity) FROM InventoryItem i WHERE i.product.sku = :sku")
    Integer sumQuantityByProductSku(String sku);

    @Query("SELECT i.quantity FROM InventoryItem i WHERE i.product.sku = :sku AND i.warehouse.id = :warehouseId")
    Optional<Integer> findQuantityByProductAndWarehouse(String sku, Long warehouseId);
}
