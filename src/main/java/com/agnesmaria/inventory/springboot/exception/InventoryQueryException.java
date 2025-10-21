package com.agnesmaria.inventory.springboot.exception;

public class InventoryQueryException extends RuntimeException {

    // ✅ Constructor baru dengan 1 parameter
    public InventoryQueryException(String message) {
        super(message);
    }

    // ✅ Constructor lama (biar kompatibel juga)
    public InventoryQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
