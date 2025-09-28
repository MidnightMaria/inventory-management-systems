package com.agnesmaria.inventory.springboot.service;

import com.agnesmaria.inventory.springboot.model.Product;
import com.agnesmaria.inventory.springboot.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductBySku(String sku) {
        return productRepository.findById(sku)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product with SKU " + sku + " not found"
                ));
    }

    @Transactional
    public Product createProduct(Product product) {
        if (productRepository.existsById(product.getSku())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product with SKU " + product.getSku() + " already exists"
            );
        }
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(String sku, Product productDetails) {
        Product product = getProductBySku(sku);
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setMinStock(productDetails.getMinStock());
        product.setQuantity(productDetails.getQuantity());
        product.setDynamicPricing(productDetails.isDynamicPricing());
        product.setCompetitorPrice(productDetails.getCompetitorPrice());
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(String sku) {
        if (!productRepository.existsById(sku)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product with SKU " + sku + " not found"
            );
        }
        productRepository.deleteById(sku);
    }
}
