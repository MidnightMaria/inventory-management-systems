package com.agnesmaria.inventory.springboot.dto;

import lombok.Data;

@Data
public class SalesRequest {
    private String sku;
    private int quantity;
}
