package com.agnesmaria.inventory.springboot.exception;

public class InventoryUpdateException extends RuntimeException {

    // ✅ Constructor baru dengan 1 parameter
    public InventoryUpdateException(String message) {
        super(message);
    }

    // ✅ Constructor lama (biar bisa digunakan di try-catch)
    public InventoryUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
