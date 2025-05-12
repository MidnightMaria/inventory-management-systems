package com.agnesmaria.inventory.springboot.repository;

import com.agnesmaria.inventory.springboot.model.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    List<InventoryMovement> findByProductSku(String sku);
    List<InventoryMovement> findByWarehouseId(Long warehouseId);
    List<InventoryMovement> findByReferenceNumber(String referenceNumber);
}