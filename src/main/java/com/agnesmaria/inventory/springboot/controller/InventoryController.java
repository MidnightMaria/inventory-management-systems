package com.agnesmaria.inventory.springboot.controller;

import com.agnesmaria.inventory.springboot.dto.InventoryRequest;
import com.agnesmaria.inventory.springboot.dto.InventoryResponse;
import com.agnesmaria.inventory.springboot.exception.InventoryQueryException;
import com.agnesmaria.inventory.springboot.exception.ProductNotFoundException;
import com.agnesmaria.inventory.springboot.exception.WarehouseNotFoundException;
import com.agnesmaria.inventory.springboot.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
    @ApiResponse(responseCode = "200", description = "Stock adjusted successfully",
            content = @Content(schema = @Schema(implementation = InventoryResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "404", description = "Product or warehouse not found")
    @ApiResponse(responseCode = "403", description = "Unauthorized access")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Inventory adjustment request",
            content = @Content(schema = @Schema(implementation = InventoryRequest.class)))
    public ResponseEntity<InventoryResponse> adjustStock(
            @Valid @RequestBody InventoryRequest request) {
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
    @Operation(summary = "Get total stock by product",
            description = "Returns the sum of stock across all warehouses for a product")
    @Parameter(name = "sku", description = "SKU of the product to retrieve total stock for", required = true)
    @ApiResponse(responseCode = "200", description = "Total stock retrieved successfully",
            content = @Content(schema = @Schema(implementation = Integer.class)))
    @ApiResponse(responseCode = "404", description = "Product not found")
    @ApiResponse(responseCode = "500", description = "Error retrieving total stock")
    public ResponseEntity<Integer> getTotalStockByProduct(
            @PathVariable String sku) {
        try {
            Integer totalStock = inventoryService.getTotalStockByProduct(sku);
            if (totalStock == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return ResponseEntity.ok(totalStock);
        } catch (InventoryQueryException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/product/{sku}/warehouse/{warehouseId}")
    @Operation(summary = "Get warehouse stock",
            description = "Returns the current stock level in a specific warehouse")
    @Parameter(name = "sku", description = "SKU of the product", required = true)
    @Parameter(name = "warehouseId", description = "ID of the warehouse", required = true)
    @ApiResponse(responseCode = "200", description = "Warehouse stock retrieved successfully",
            content = @Content(schema = @Schema(implementation = Integer.class)))
    @ApiResponse(responseCode = "404", description = "Product or warehouse not found")
    @ApiResponse(responseCode = "500", description = "Error retrieving warehouse stock")
    public ResponseEntity<Integer> getWarehouseStock(
            @PathVariable String sku,
            @PathVariable Long warehouseId) {
        try {
            Integer stock = inventoryService.getStockByProductAndWarehouse(sku, warehouseId);
            return ResponseEntity.ok(stock);
        } catch (InventoryQueryException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/export")
    @Operation(summary = "Export full inventory data for analytics")
    public ResponseEntity<List<InventoryResponse>> exportAllInventory() {
    return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @PostMapping("/reduce-stock")
    public ResponseEntity<InventoryResponse> reduceStock(@Valid @RequestBody InventoryRequest request) {
    InventoryResponse response = inventoryService.reduceStock(request);
    return ResponseEntity.ok(response);
    }
}