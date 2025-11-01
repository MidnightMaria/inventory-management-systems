package com.agnesmaria.inventory.springboot.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransferRequest {
    private Long fromWarehouseId;
    private Long toWarehouseId;
    private String productSku;
    private Integer quantity;
    private String reference;
}
