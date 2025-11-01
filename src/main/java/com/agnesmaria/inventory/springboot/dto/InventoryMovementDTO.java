package com.agnesmaria.inventory.springboot.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovementDTO {
    private Long id;
    private String productSku;
    private String productName;
    private String warehouseCode;
    private String movementType;
    private Integer difference;
    private String reason;
    private String referenceNumber;
    private String performedBy;
    private LocalDateTime createdAt;
}
