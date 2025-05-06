package com.agnesmaria.inventory.springboot.controller;

import com.agnesmaria.inventory.springboot.dto.InventoryRequest;
import com.agnesmaria.inventory.springboot.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping("/adjust-stock")
    public void adjustStock(@Valid @RequestBody InventoryRequest request) {
        inventoryService.updateStock(request);
    }
}