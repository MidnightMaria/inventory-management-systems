package com.agnesmaria.inventory.springboot.controller;

import com.agnesmaria.inventory.springboot.dto.PurchaseOrderRequest;
import com.agnesmaria.inventory.springboot.dto.PurchaseOrderResponse; // Import DTO yang baru
import com.agnesmaria.inventory.springboot.model.PurchaseOrder;
import com.agnesmaria.inventory.springboot.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService poService;

    @PostMapping
    public PurchaseOrderResponse createPO(@RequestBody PurchaseOrderRequest request) { // Gunakan DTO di controller
        return poService.createPO(request);
    }
}