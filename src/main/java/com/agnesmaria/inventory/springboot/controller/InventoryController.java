package com.agnesmaria.inventory.springboot.controller;

import com.agnesmaria.inventory.springboot.dto.InventoryMovementResponse;
import com.agnesmaria.inventory.springboot.dto.InventoryRequest;
import com.agnesmaria.inventory.springboot.dto.InventoryResponse;
import com.agnesmaria.inventory.springboot.model.InventoryMovement;
import com.agnesmaria.inventory.springboot.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory Management", description = "Endpoints for managing inventory stock levels and movement logs")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/adjust-stock")
    @Operation(summary = "Adjust stock manually (IN, OUT, ADJUST)")
    public ResponseEntity<InventoryResponse> adjustStock(@Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.ok(inventoryService.updateStock(request));
    }

    @PostMapping("/reduce-stock")
    @Operation(summary = "Reduce stock due to sales or shipment")
    public ResponseEntity<InventoryResponse> reduceStock(@Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.ok(inventoryService.reduceStock(request));
    }

    @GetMapping("/product/{sku}/total")
    @Operation(summary = "Get total stock by product SKU")
    public ResponseEntity<Integer> getTotalStock(@PathVariable String sku) {
        return ResponseEntity.ok(inventoryService.getTotalStockByProduct(sku));
    }

    @GetMapping("/product/{sku}/warehouse/{warehouseId}")
    @Operation(summary = "Get stock in specific warehouse")
    public ResponseEntity<Integer> getStockByWarehouse(
            @PathVariable String sku,
            @PathVariable Long warehouseId) {
        return ResponseEntity.ok(inventoryService.getStockByProductAndWarehouse(sku, warehouseId));
    }

    @GetMapping("/export")
    @Operation(summary = "Export all inventory data for analytics")
    public ResponseEntity<List<InventoryResponse>> exportInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    // 🔹 Endpoint untuk Supply Chain Service
    @PostMapping("/internal/adjust-stock")
    @Operation(summary = "Internal: Adjust stock after purchase order is received")
    public ResponseEntity<InventoryResponse> adjustStockInternal(@RequestBody InventoryRequest request) {
        return ResponseEntity.ok(inventoryService.updateStockInternal(request));
    }

    // 🧾 Get all inventory movement logs
    @GetMapping("/movements")
    @Operation(summary = "Get all inventory movement logs", description = "Retrieve all stock movement records")
    public ResponseEntity<List<InventoryMovement>> getAllMovements() {
        return ResponseEntity.ok(inventoryService.getAllMovements());
    }

    // 📤 Export inventory movements for analytics
    @GetMapping("/movements/export")
    @Operation(summary = "Export inventory movement data", description = "Export historical movement data for analytics or forecasting")
    public ResponseEntity<List<InventoryMovement>> exportMovements() {
        return ResponseEntity.ok(inventoryService.exportMovements());
    }

    @GetMapping("/movements/export/summary")
    @Operation(summary = "Export summarized movement data", 
            description = "Simplified version for forecasting or analytics")
    public ResponseEntity<List<InventoryMovementResponse>> exportMovementSummary() {
        return ResponseEntity.ok(inventoryService.exportMovementSummary());
    }
}
