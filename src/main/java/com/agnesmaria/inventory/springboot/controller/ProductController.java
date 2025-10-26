package com.agnesmaria.inventory.springboot.controller;

import com.agnesmaria.inventory.springboot.dto.ProductRequest;
import com.agnesmaria.inventory.springboot.model.Product;
import com.agnesmaria.inventory.springboot.service.ProductService;
import com.agnesmaria.inventory.springboot.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Endpoints for managing product catalog and stock levels")
public class ProductController {

    private final ProductService productService;
    private final InventoryService inventoryService;

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve a list of all registered products")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{sku}")
    @Operation(summary = "Get product by SKU", description = "Retrieve a single product using its SKU")
    public Product getProduct(@PathVariable String sku) {
        return productService.getProductBySku(sku);
    }

    @PostMapping
    @Operation(summary = "Create a new product", description = "Register a new product in the system")
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
    @Operation(summary = "Update existing product", description = "Modify details of an existing product by SKU")
    public Product updateProduct(
            @PathVariable String sku,
            @Valid @RequestBody ProductRequest productRequest) {

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
    @Operation(summary = "Delete a product", description = "Remove a product by its SKU")
    public void deleteProduct(@PathVariable String sku) {
        productService.deleteProduct(sku);
    }

    @GetMapping("/{sku}/stock")
    @Operation(summary = "Get total stock for a product", description = "Retrieve total stock across all warehouses for a given SKU")
    public int getTotalStock(@PathVariable String sku) {
        return inventoryService.getTotalStockByProduct(sku);
    }

    @GetMapping("/{sku}/stock-level")
    @Operation(summary = "Get stock level for a product", description = "Retrieve the current stock quantity recorded in the product table")
    public int getStockLevel(@PathVariable String sku) {
        return productService.getProductBySku(sku).getQuantity(); // ✅ FIXED
    }

    @GetMapping("/export")
    @Operation(summary = "Export all products", description = "Export all product data for reporting or analytics")
    public List<Product> exportProducts() {
        return productService.getAllProducts();
    }
}