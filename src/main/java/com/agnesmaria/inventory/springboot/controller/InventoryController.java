package com.agnesmaria.inventory.springboot.controller;

import com.agnesmaria.inventory.springboot.dto.InventoryRequest;
import com.agnesmaria.inventory.springboot.dto.InventoryResponse;
import com.agnesmaria.inventory.springboot.exception.InventoryQueryException;
import com.agnesmaria.inventory.springboot.exception.ProductNotFoundException;
import com.agnesmaria.inventory.springboot.exception.WarehouseNotFoundException;
import com.agnesmaria.inventory.springboot.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory Management", description = "APIs for managing inventory stock levels")
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping("/adjust-stock")
    @Operation(summary = "Adjust inventory stock level",
            description = "Update stock quantity for a product in a warehouse")
    public ResponseEntity<InventoryResponse> adjustStock(@Valid @RequestBody InventoryRequest request) {
        try {
            InventoryResponse response = inventoryService.updateStock(request);
            return ResponseEntity.ok(response);
        } catch (ProductNotFoundException | WarehouseNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/product/{sku}/total")
    public ResponseEntity<Integer> getTotalStockByProduct(@PathVariable String sku) {
        try {
            Integer totalStock = inventoryService.getTotalStockByProduct(sku);
            return (totalStock != null) ? ResponseEntity.ok(totalStock)
                    : new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (InventoryQueryException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/product/{sku}/warehouse/{warehouseId}")
    public ResponseEntity<Integer> getWarehouseStock(
            @PathVariable String sku, @PathVariable Long warehouseId) {
        try {
            return ResponseEntity.ok(inventoryService.getStockByProductAndWarehouse(sku, warehouseId));
        } catch (InventoryQueryException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/export")
    public ResponseEntity<List<InventoryResponse>> exportAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @PostMapping("/reduce-stock")
    public ResponseEntity<InventoryResponse> reduceStock(@Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.ok(inventoryService.reduceStock(request));
    }

    // Internal endpoint untuk Supply Chain Service
    @PostMapping("/internal/adjust-stock")
    public ResponseEntity<InventoryResponse> adjustStockInternal(@Valid @RequestBody InventoryRequest request) {
        InventoryResponse response = inventoryService.updateStockInternal(request);
        return ResponseEntity.ok(response);
    }
}
