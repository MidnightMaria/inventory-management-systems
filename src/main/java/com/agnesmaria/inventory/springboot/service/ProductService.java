package com.agnesmaria.inventory.springboot.service;

import com.agnesmaria.inventory.springboot.model.Product;
import com.agnesmaria.inventory.springboot.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductBySku(String sku) {
        return productRepository.findById(sku)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Transactional
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(String sku, Product productDetails) {
        Product product = getProductBySku(sku);
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setMinStock(productDetails.getMinStock());
        product.setDynamicPricing(productDetails.isDynamicPricing());
        product.setCompetitorPrice(productDetails.getCompetitorPrice());
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(String sku) {
        productRepository.deleteById(sku);
    }
}