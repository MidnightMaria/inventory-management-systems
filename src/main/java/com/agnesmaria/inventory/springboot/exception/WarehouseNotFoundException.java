package com.agnesmaria.inventory.springboot.exception;

public class WarehouseNotFoundException extends RuntimeException {
    public WarehouseNotFoundException(Long warehouseId) {
        super("Warehouse not found with ID: " + warehouseId);
    }
}