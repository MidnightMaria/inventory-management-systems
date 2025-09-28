package com.agnesmaria.inventory.springboot.repository;

import com.agnesmaria.inventory.springboot.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    // Custom query methods bisa ditambahkan di sini jika diperlukan
}