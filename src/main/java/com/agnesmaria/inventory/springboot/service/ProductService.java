package com.agnesmaria.inventory.springboot.service;

import com.agnesmaria.inventory.springboot.exception.WarehouseNotFoundException;
import com.agnesmaria.inventory.springboot.model.InventoryItem;
import com.agnesmaria.inventory.springboot.model.Product;
import com.agnesmaria.inventory.springboot.model.Warehouse;
import com.agnesmaria.inventory.springboot.repository.InventoryItemRepository;
import com.agnesmaria.inventory.springboot.repository.ProductRepository;
import com.agnesmaria.inventory.springboot.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryItemRepository inventoryItemRepository;

    // 📦 Get all products
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // 🔍 Get product by SKU
    public Product getProductBySku(String sku) {
        return productRepository.findById(sku)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product with SKU " + sku + " not found"
                ));
    }

    // 🆕 Create new product + initialize stock in default warehouse
    @Transactional
    public Product createProduct(Product product) {
        if (productRepository.existsById(product.getSku())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product with SKU " + product.getSku() + " already exists"
            );
        }

        // 💾 Save product first
        Product savedProduct = productRepository.save(product);

        // 🏭 Initialize stock in default warehouse (WH-001 or ID=1)
        try {
            Warehouse defaultWarehouse = warehouseRepository.findById(1L)
                    .orElseThrow(() -> new WarehouseNotFoundException(1L));

            InventoryItem item = InventoryItem.builder()
                    .product(savedProduct)
                    .warehouse(defaultWarehouse)
                    .quantity(product.getQuantity())
                    .build();

            inventoryItemRepository.save(item);

            log.info("✅ Initialized stock for {} in warehouse {} (Qty: {})",
                    savedProduct.getSku(), defaultWarehouse.getCode(), product.getQuantity());

        } catch (WarehouseNotFoundException e) {
            log.warn("⚠️ Default warehouse (ID=1) not found. Product {} created without initial stock record.",
                    product.getSku());
        } catch (Exception e) {
            log.error("❌ Failed to initialize inventory for {}: {}", product.getSku(), e.getMessage());
        }

        return savedProduct;
    }

    // ✏️ Update existing product
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

        log.info("📝 Product updated: {} ({})", sku, product.getName());
        return productRepository.save(product);
    }

    // ❌ Delete product (safe)
    @Transactional
    public void deleteProduct(String sku) {
        try {
            if (!productRepository.existsById(sku)) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product with SKU " + sku + " not found"
                );
            }
            productRepository.deleteById(sku);
            log.info("🗑️ Product {} deleted successfully", sku);

        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot delete product " + sku + " because it is still referenced in inventory or movement records."
            );
        }
    }
}
