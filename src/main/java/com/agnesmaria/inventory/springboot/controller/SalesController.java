package com.agnesmaria.inventory.springboot.controller;

import com.agnesmaria.inventory.springboot.dto.SalesReportResponse;
import com.agnesmaria.inventory.springboot.model.Sales;
import com.agnesmaria.inventory.springboot.service.SalesReportService;
import com.agnesmaria.inventory.springboot.service.SalesService;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;
    private final SalesReportService salesReportService;

    @PostMapping
    public ResponseEntity<Sales> createSale(@RequestBody SalesRequest request) {
        Sales sale = salesService.createSale(request.getSku(), request.getQuantity());
        return ResponseEntity.ok(sale);
    }

    @GetMapping
    public ResponseEntity<List<Sales>> getAllSales() {
        return ResponseEntity.ok(salesService.getAllSales());
    }

    @GetMapping("/{sku}")
    public ResponseEntity<List<Sales>> getSalesBySku(@PathVariable String sku) {
        return ResponseEntity.ok(salesService.getSalesBySku(sku));
    }

    // NEW: laporan total penjualan per produk
    @GetMapping("/reports/total-per-product")
    public ResponseEntity<List<SalesReportResponse>> getTotalSalesPerProduct() {
        return ResponseEntity.ok(salesReportService.getTotalSalesPerProduct());
    }

    // NEW: laporan penjualan harian
    @GetMapping("/reports/daily-sales")
    public ResponseEntity<List<SalesReportResponse>> getDailySales() {
        return ResponseEntity.ok(salesReportService.getDailySales());
    }

    @Data
    static class SalesRequest {
        private String sku;
        private int quantity;
    }
}
