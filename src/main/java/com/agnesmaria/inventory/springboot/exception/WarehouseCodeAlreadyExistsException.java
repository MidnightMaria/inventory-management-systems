package com.agnesmaria.inventory.springboot.exception;

public class WarehouseCodeAlreadyExistsException extends RuntimeException {
    public WarehouseCodeAlreadyExistsException(String code) {
        super("Warehouse code already exists: " + code);
    }
}