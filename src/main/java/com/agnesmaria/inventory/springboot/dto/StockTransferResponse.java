package com.agnesmaria.inventory.springboot.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransferResponse {
    private String status;
    private String message;
    private String productSku;
    private Integer quantity;
    private Long fromWarehouseId;
    private Long toWarehouseId;
}
