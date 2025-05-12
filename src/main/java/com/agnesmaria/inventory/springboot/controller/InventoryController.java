package com.agnesmaria.inventory.springboot.controller;

import com.agnesmaria.inventory.springboot.dto.InventoryRequest;
import com.agnesmaria.inventory.springboot.dto.InventoryResponse;
import com.agnesmaria.inventory.springboot.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory Management", description = "APIs for managing inventory stock levels")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping("/adjust-stock")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Adjust inventory stock level",
               description = "Update the stock quantity for a product in a specific warehouse")
    @ApiResponse(responseCode = "200", description = "Stock adjusted successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "404", description = "Product or warehouse not found")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    public ResponseEntity<InventoryResponse> adjustStock(
            @Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.ok(inventoryService.updateStock(request));
    }

    @GetMapping("/product/{sku}/total")
    @Operation(summary = "Get total stock by product",
               description = "Returns the sum of stock across all warehouses for a product")
    public ResponseEntity<Integer> getTotalStockByProduct(
            @PathVariable String sku) {
        return ResponseEntity.ok(inventoryService.getTotalStockByProduct(sku));
    }

    @GetMapping("/product/{sku}/warehouse/{warehouseId}")
    @Operation(summary = "Get warehouse stock",
               description = "Returns the current stock level in a specific warehouse")
    public ResponseEntity<Integer> getWarehouseStock(
            @PathVariable String sku,
            @PathVariable Long warehouseId) {
        return ResponseEntity.ok(
                inventoryService.getStockByProductAndWarehouse(sku, warehouseId));
    }
}