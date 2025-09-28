package com.agnesmaria.inventory.springboot.controller;

import com.agnesmaria.inventory.springboot.dto.ProductRequest;
import com.agnesmaria.inventory.springboot.model.Product;
import com.agnesmaria.inventory.springboot.service.ProductService;
import com.agnesmaria.inventory.springboot.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@Tag(name = "Products", description = "APIs for product management")
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final InventoryService inventoryService;

    @Operation(
            summary = "Get all products",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{sku}")
    public Product getProduct(@PathVariable String sku) {
        return productService.getProductBySku(sku);
    }

    @PostMapping
    public Product createProduct(@Valid @RequestBody ProductRequest productRequest) {
        Product product = Product.builder()
                .sku(productRequest.getSku())
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .minStock(productRequest.getMinStock())
                .quantity(productRequest.getQuantity())
                .dynamicPricing(productRequest.isDynamicPricing())
                .competitorPrice(productRequest.getCompetitorPrice())
                .build();
        return productService.createProduct(product);
    }

    @PutMapping("/{sku}")
    public Product updateProduct(@PathVariable String sku, @Valid @RequestBody ProductRequest productRequest) {
        Product product = Product.builder()
                .sku(sku)
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .minStock(productRequest.getMinStock())
                .quantity(productRequest.getQuantity())
                .dynamicPricing(productRequest.isDynamicPricing())
                .competitorPrice(productRequest.getCompetitorPrice())
                .build();
        return productService.updateProduct(sku, product);
    }

    @DeleteMapping("/{sku}")
    public void deleteProduct(@PathVariable String sku) {
        productService.deleteProduct(sku);
    }

    @GetMapping("/{sku}/stock")
    public int getTotalStock(@PathVariable String sku) {
        return inventoryService.getTotalStockByProduct(sku);
    }
}
