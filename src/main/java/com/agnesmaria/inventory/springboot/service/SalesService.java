package com.agnesmaria.inventory.springboot.service;

import com.agnesmaria.inventory.springboot.model.Product;
import com.agnesmaria.inventory.springboot.model.Sales;
import com.agnesmaria.inventory.springboot.repository.ProductRepository;
import com.agnesmaria.inventory.springboot.repository.SalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesService {

    private final SalesRepository salesRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Sales createSale(String sku, int quantity) {
        Product product = productRepository.findById(sku)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product with SKU " + sku + " not found"));

        if (product.getStock() < quantity) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Insufficient stock for product " + sku);
        }

        // Kurangi stok
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);

        // Buat sales
        Sales sales = Sales.builder()
                .product(product)
                .quantity(quantity)
                .timestamp(LocalDateTime.now())
                .build();

        return salesRepository.save(sales);
    }

    public List<Sales> getAllSales() {
        return salesRepository.findAll();
    }

    public List<Sales> getSalesBySku(String sku) {
        Product product = productRepository.findById(sku)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Product not found with SKU: " + sku));

        return salesRepository.findByProduct(product);
    }
}
