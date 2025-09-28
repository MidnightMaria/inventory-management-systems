package com.agnesmaria.inventory.springboot.repository;

import com.agnesmaria.inventory.springboot.model.Product;
import com.agnesmaria.inventory.springboot.model.Sales;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesRepository extends JpaRepository<Sales, Long> {
    List<Sales> findByProduct(Product product);
}
