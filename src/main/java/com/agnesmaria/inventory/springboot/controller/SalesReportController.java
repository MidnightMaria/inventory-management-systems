package com.agnesmaria.inventory.springboot.controller;

import com.agnesmaria.inventory.springboot.dto.SalesReportResponse;
import com.agnesmaria.inventory.springboot.service.SalesReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/sales/report")
@RequiredArgsConstructor
public class SalesReportController {

    private final SalesReportService salesReportService;
    private final ObjectMapper objectMapper = new ObjectMapper(); // untuk JSON export

    /**
     * Export total sales per product as CSV
     */
    @GetMapping("/total-per-product/csv")
    public ResponseEntity<byte[]> exportTotalSalesPerProductCsv() {
        List<SalesReportResponse> reports = salesReportService.getTotalSalesPerProduct();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8);

        writer.println("SKU,Product Name,Total Quantity,Total Revenue");

        for (SalesReportResponse report : reports) {
            writer.printf("%s,%s,%d,%s%n",
                    report.getSku(),
                    report.getProductName(),
                    report.getTotalQuantity(),
                    report.getTotalRevenue());
        }

        writer.flush();
        byte[] csvBytes = out.toByteArray();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=total_sales_per_product.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }

    /**
     * Export daily sales as CSV
     */
    @GetMapping("/daily-sales/csv")
    public ResponseEntity<byte[]> exportDailySalesCsv() {
        List<SalesReportResponse> reports = salesReportService.getDailySales();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8);

        writer.println("Date,SKU,Product Name,Total Quantity,Total Revenue");

        for (SalesReportResponse report : reports) {
            writer.printf("%s,%s,%s,%d,%s%n",
                    report.getDate(),
                    report.getSku(),
                    report.getProductName(),
                    report.getTotalQuantity(),
                    report.getTotalRevenue());
        }

        writer.flush();
        byte[] csvBytes = out.toByteArray();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=daily_sales.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }

    /**
     * Export sales as JSON
     */
    @GetMapping("/export/json")
    public ResponseEntity<byte[]> exportSalesJson(@RequestParam(defaultValue = "daily") String type) {
        try {
            List<SalesReportResponse> reports;
            String filename;

            if (type.equalsIgnoreCase("total")) {
                reports = salesReportService.getTotalSalesPerProduct();
                filename = "total_sales.json";
            } else {
                reports = salesReportService.getDailySales();
                filename = "daily_sales.json";
            }

            byte[] jsonBytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(reports);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(("Error generating JSON: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }
}
