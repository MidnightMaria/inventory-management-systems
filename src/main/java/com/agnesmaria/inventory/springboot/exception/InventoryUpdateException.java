package com.agnesmaria.inventory.springboot.exception;

public class InventoryUpdateException extends RuntimeException {
    public InventoryUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}