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

    @Pattern(regexp = "IN|OUT|ADJUST|ADJUSTMENT|TRANSFER",
             message = "Movement type must be IN, OUT, ADJUST, ADJUSTMENT, or TRANSFER")
    private String movementType;

    private String referenceNumber;

    private Long toWarehouseId; // optional for TRANSFER
}
