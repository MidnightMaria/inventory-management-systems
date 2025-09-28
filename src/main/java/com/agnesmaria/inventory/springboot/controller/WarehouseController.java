package com.agnesmaria.inventory.springboot.controller;

import com.agnesmaria.inventory.springboot.dto.WarehouseRequest;
import com.agnesmaria.inventory.springboot.dto.WarehouseResponse;
import com.agnesmaria.inventory.springboot.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@Tag(name = "Warehouse Management", description = "APIs for managing warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    @Operation(summary = "Create a new warehouse")
    @ApiResponse(responseCode = "201", description = "Warehouse created successfully",
            content = @Content(schema = @Schema(implementation = WarehouseResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "409", description = "Warehouse code already exists")
    public ResponseEntity<WarehouseResponse> createWarehouse(
            @Valid @RequestBody WarehouseRequest request) {
        WarehouseResponse response = warehouseService.createWarehouse(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all warehouses")
    @ApiResponse(responseCode = "200", description = "List of all warehouses",
            content = @Content(schema = @Schema(type = "array", implementation = WarehouseResponse.class)))
    public ResponseEntity<List<WarehouseResponse>> getAllWarehouses() {
        return ResponseEntity.ok(warehouseService.getAllWarehouses());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a warehouse by ID")
    @ApiResponse(responseCode = "200", description = "Warehouse found",
            content = @Content(schema = @Schema(implementation = WarehouseResponse.class)))
    @ApiResponse(responseCode = "404", description = "Warehouse not found")
    @Parameter(name = "id", description = "ID of the warehouse to retrieve", required = true)
    public ResponseEntity<WarehouseResponse> getWarehouseById(
            @PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.getWarehouseById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a warehouse by ID")
    @ApiResponse(responseCode = "200", description = "Warehouse updated successfully",
            content = @Content(schema = @Schema(implementation = WarehouseResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Warehouse not found")
    @ApiResponse(responseCode = "409", description = "Warehouse code already exists")
    @Parameter(name = "id", description = "ID of the warehouse to update", required = true)
    public ResponseEntity<WarehouseResponse> updateWarehouse(
            @PathVariable Long id,
            @Valid @RequestBody WarehouseRequest request) {
        return ResponseEntity.ok(warehouseService.updateWarehouse(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Toggle the active status of a warehouse")
    @ApiResponse(responseCode = "204", description = "Warehouse status updated successfully")
    @ApiResponse(responseCode = "404", description = "Warehouse not found")
    @Parameter(name = "id", description = "ID of the warehouse to toggle status", required = true)
    public ResponseEntity<Void> toggleWarehouseStatus(
            @PathVariable Long id) {
        warehouseService.toggleWarehouseStatus(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active warehouses")
    @ApiResponse(responseCode = "200", description = "List of all active warehouses",
            content = @Content(schema = @Schema(type = "array", implementation = WarehouseResponse.class)))
    public ResponseEntity<List<WarehouseResponse>> getActiveWarehouses() {
        return ResponseEntity.ok(warehouseService.getAllActiveWarehouses()); // Service bertanggung jawab memfilter
    }
}