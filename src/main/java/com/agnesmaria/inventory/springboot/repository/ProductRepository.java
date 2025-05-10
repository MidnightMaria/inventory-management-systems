package com.agnesmaria.inventory.springboot.repository;

import com.agnesmaria.inventory.springboot.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {
    @Query("SELECT p FROM Product p WHERE p.quantity < p.minStock")
    List<Product> findByQuantityLessThanMinStock();
    
    // Tambahkan method untuk mencari berdasarkan SKU
    Optional<Product> findBySku(String sku);
}