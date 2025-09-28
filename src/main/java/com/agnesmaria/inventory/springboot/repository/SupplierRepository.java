package com.agnesmaria.inventory.springboot.repository;

import com.agnesmaria.inventory.springboot.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    boolean existsByName(String name);
    boolean existsByEmail(String email);
}