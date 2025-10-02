package com.agnesmaria.inventory.springboot.service;

import com.agnesmaria.inventory.springboot.dto.SalesHistoryResponse;
import com.agnesmaria.inventory.springboot.dto.SalesReportResponse;
import com.agnesmaria.inventory.springboot.model.Sales;
import com.agnesmaria.inventory.springboot.repository.SalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


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
    public List<SalesHistoryResponse> getAllSalesHistory() {
        return mapToResponse(salesRepository.findAllWithProduct());
    }

    public List<SalesHistoryResponse> getSalesHistoryByDateRange(LocalDateTime start, LocalDateTime end) {
        return mapToResponse(salesRepository.findByDateRange(start, end));
    }

    public List<SalesHistoryResponse> getSalesHistoryBySku(String sku) {
        return mapToResponse(salesRepository.findBySku(sku));
    }

    public List<SalesHistoryResponse> getSalesHistoryBySkuAndDateRange(String sku, LocalDateTime start, LocalDateTime end) {
        return mapToResponse(salesRepository.findBySkuAndDateRange(sku, start, end));
    }

    private List<SalesHistoryResponse> mapToResponse(List<Sales> salesList) {
        return salesList.stream()
                .map(s -> SalesHistoryResponse.builder()
                        .id(s.getId())
                        .sku(s.getProduct().getSku())
                        .productName(s.getProduct().getName())
                        .quantity(s.getQuantity())
                        .price(s.getPrice())
                        .total(s.getPrice().multiply(
                                java.math.BigDecimal.valueOf(s.getQuantity())))
                        .timestamp(s.getTimestamp())
                        .build())
                .collect(Collectors.toList());
    }
}
