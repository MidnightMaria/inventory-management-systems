package com.agnesmaria.inventory.springboot.exception;

public class InventoryQueryException extends RuntimeException {
    public InventoryQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}