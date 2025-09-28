package com.agnesmaria.inventory.springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponse {
    private Long id;
    private Long supplierId;
    private LocalDateTime orderDate;
    private String status; // Gunakan String untuk status, atau enum DTO jika diperlukan
    private List<OrderItemResponse> items;
}