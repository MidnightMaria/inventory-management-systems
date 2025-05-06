package com.agnesmaria.inventory.springboot.dto;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
public class InventoryRequest {
    @NotBlank
    private String productSku;
    
    @NotNull
    private Long warehouseId;
    
    @Min(0)
    private int quantity;
}