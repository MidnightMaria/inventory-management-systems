package com.agnesmaria.inventory.springboot.repository;

import com.agnesmaria.inventory.springboot.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {
    // Custom queries can be added here later
}