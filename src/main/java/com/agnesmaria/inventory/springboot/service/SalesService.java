package com.agnesmaria.inventory.springboot.service;

import com.agnesmaria.inventory.springboot.dto.SalesRequest;
import com.agnesmaria.inventory.springboot.model.Product;
import com.agnesmaria.inventory.springboot.model.Sales;
import com.agnesmaria.inventory.springboot.repository.ProductRepository;
import com.agnesmaria.inventory.springboot.repository.SalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesService {

    private final SalesRepository salesRepository;
    private final ProductRepository productRepository;

    public Sales createSale(SalesRequest request) {
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        Product product = productRepository.findBySku(request.getSku())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with SKU: " + request.getSku()));

        if (product.getQuantity() < request.getQuantity()) {
            throw new IllegalArgumentException("Not enough stock for product: " + product.getName());
        }

        // Kurangi stok produk
        product.setQuantity(product.getQuantity() - request.getQuantity());
        productRepository.save(product);

        // Catat sales
        Sales sales = Sales.builder()
                .product(product)
                .quantity(request.getQuantity())
                .timestamp(LocalDateTime.now())
                .build();

        return salesRepository.save(sales);
    }

    public List<Sales> getAllSales() {
        return salesRepository.findAll();
    }

    public List<Sales> getSalesBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with SKU: " + sku));
        return salesRepository.findByProduct(product);
    }
}
