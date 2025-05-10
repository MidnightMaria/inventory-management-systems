package com.agnesmaria.inventory.springboot.service;

import com.agnesmaria.inventory.springboot.dto.PurchaseOrderRequest;
import com.agnesmaria.inventory.springboot.model.*;
import com.agnesmaria.inventory.springboot.repository.ProductRepository;
import com.agnesmaria.inventory.springboot.repository.PurchaseOrderRepository;
import com.agnesmaria.inventory.springboot.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;

    @Transactional
    public PurchaseOrder createPO(PurchaseOrderRequest request) {
        // Validasi supplier
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        
        // Buat PO
        PurchaseOrder po = PurchaseOrder.builder()
                .supplier(supplier)
                .status(PurchaseOrderStatus.DRAFT)
                .orderDate(LocalDateTime.now())
                .build();
        
        // Buat PO Items
        List<PurchaseOrderItem> items = request.getItems().stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductSku())
                            .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProductSku()));
                    
                    return PurchaseOrderItem.builder()
                            .purchaseOrder(po)
                            .product(product)
                            .quantity(item.getQuantity())
                            .unitPrice(product.getPrice())
                            .build();
                })
                .collect(Collectors.toList());
        
        po.setItems(items);
        
        return purchaseOrderRepository.save(po);
    }
}