package com.agnesmaria.inventory.springboot.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotBlank
    private String sku;
    
    @NotBlank
    private String name;
    
    private String description;
    
    @NotNull
    @Positive
    private BigDecimal price;
    
    @Min(0)
    private int minStock;
    
    private boolean dynamicPricing;
    
    @Positive
    private BigDecimal competitorPrice;
}