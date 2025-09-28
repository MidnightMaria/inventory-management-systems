package com.agnesmaria.inventory.springboot.repository;

import com.agnesmaria.inventory.springboot.model.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    
    // Add this method to check if a warehouse code exists
    boolean existsByCode(String code);
    
    // Optional: You might also want these methods
    Optional<Warehouse> findByCode(String code);
    List<Warehouse> findByIsActiveTrue();
    List<Warehouse> findByNameContainingIgnoreCase(String name);
}