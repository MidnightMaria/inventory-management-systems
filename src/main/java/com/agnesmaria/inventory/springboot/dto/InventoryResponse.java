package com.agnesmaria.inventory.springboot.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryResponse {
    private String productSku;
    private String productName;
    private String warehouseCode;
    private Integer previousStock;
    private Integer currentStock;
    private Integer difference;
    private String movementType;
    private LocalDateTime updatedAt;
}