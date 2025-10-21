package com.agnesmaria.inventory.springboot.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
    
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private String productSku;
    private int quantity;
    private BigDecimal unitPrice;
}