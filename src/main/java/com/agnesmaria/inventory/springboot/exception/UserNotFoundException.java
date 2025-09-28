package com.agnesmaria.inventory.springboot.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String identifier, String field) {
        super("User not found with " + field + ": " + identifier);
    }

    // Overload untuk tetap mendukung pesan username saja
    public UserNotFoundException(String username) {
        super("User not found with username: " + username);
    }
}