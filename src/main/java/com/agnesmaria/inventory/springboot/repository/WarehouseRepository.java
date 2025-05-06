package com.agnesmaria.inventory.springboot.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.agnesmaria.inventory.springboot.model.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByCode(String code); 
}