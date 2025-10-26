package com.agnesmaria.inventory.springboot.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovementResponse {
    private String productSku;
    private String warehouseCode;
    private String movementType;
    private Integer difference;
    private String reason;
    private String referenceNumber;
    private LocalDateTime createdAt;
}
