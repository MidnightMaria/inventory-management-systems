package com.agnesmaria.inventory.springboot.service;

import com.agnesmaria.inventory.springboot.dto.SalesReportResponse;
import com.agnesmaria.inventory.springboot.repository.SalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesReportService {

    private final SalesRepository salesRepository;

    // Laporan total penjualan per produk
    public List<SalesReportResponse> getTotalSalesPerProduct() {
        List<Object[]> results = salesRepository.getTotalSalesPerProduct();
        return results.stream()
                .map(row -> SalesReportResponse.builder()
                        .sku((String) row[0])
                        .productName((String) row[1])
                        .totalQuantity(((Number) row[2]).intValue())
                        .totalRevenue((BigDecimal) row[3])
                        .key((String) row[0]) // pakai SKU sebagai key
                        .build())
                .toList();
    }

    // Laporan penjualan harian
    public List<SalesReportResponse> getDailySales() {
        List<Object[]> results = salesRepository.getDailySales();
        return results.stream()
                .map(row -> SalesReportResponse.builder()
                        .date((LocalDate) row[0])
                        .sku((String) row[1])
                        .productName((String) row[2])
                        .totalQuantity(((Number) row[3]).intValue())
                        .totalRevenue((BigDecimal) row[4])
                        .key(((LocalDate) row[0]).toString()) // pakai tanggal sebagai key
                        .build())
                .toList();
    }
}
