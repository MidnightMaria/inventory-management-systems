package com.agnesmaria.inventory.springboot.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesHistoryResponse {
    private Long id;              // ID transaksi
    private String sku;           // SKU produk
    private String productName;   // Nama produk
    private int quantity;         // Jumlah terjual
    private BigDecimal price;     // Harga per unit
    private BigDecimal total;     // quantity * price
    private LocalDateTime timestamp; // Waktu transaksi
}
