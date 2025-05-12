package com.agnesmaria.inventory.springboot.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequest {
    @NotBlank(message = "Product SKU is required")
    private String productSku;
    
    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;
    
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;
    
    @NotBlank(message = "Adjustment reason is required")
    private String adjustmentReason;
    
    @NotBlank(message = "Movement type is required")
    @Pattern(regexp = "IN|OUT|ADJUSTMENT|TRANSFER", 
             message = "Movement type must be IN, OUT, ADJUSTMENT, or TRANSFER")
    private String movementType;
    
    private String referenceNumber;
}