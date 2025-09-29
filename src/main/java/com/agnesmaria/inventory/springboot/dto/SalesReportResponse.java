package com.agnesmaria.inventory.springboot.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportResponse {
    private String sku;
    private String productName;
    private int totalQuantity;      // total qty terjual
    private BigDecimal totalRevenue; // total pendapatan
    private LocalDate date;         // optional, kalau agregasi harian
    private String key;       // bisa sku atau tanggal
    private Long totalSales;  // jumlah penjualan
}
