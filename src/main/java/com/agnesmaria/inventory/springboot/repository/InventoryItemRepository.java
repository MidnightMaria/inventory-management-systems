package com.agnesmaria.inventory.springboot.repository;

import com.agnesmaria.inventory.springboot.model.InventoryItem;
import com.agnesmaria.inventory.springboot.model.Product;
import com.agnesmaria.inventory.springboot.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    Optional<InventoryItem> findByProductAndWarehouse(Product product, Warehouse warehouse);
    @Query("SELECT SUM(i.quantity) FROM InventoryItem i WHERE i.product.sku = :sku")
    int sumQuantityByProductSku(@Param("sku") String sku);
}