package com.agnesmaria.inventory.springboot.repository;

import com.agnesmaria.inventory.springboot.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}