package com.agnesmaria.inventory.springboot.service;

import com.agnesmaria.inventory.springboot.dto.SalesReportResponse;
import com.agnesmaria.inventory.springboot.repository.SalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesExportService {

    private final SalesReportService salesReportService;
    private final SalesRepository salesRepository;

    /**
     * Export total sales per product to CSV format
     */
    public String exportTotalSalesCsv() {
        List<SalesReportResponse> report = salesReportService.getTotalSalesPerProduct();

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        // Header
        pw.println("SKU,ProductName,TotalQuantity");

        // Data
        for (SalesReportResponse r : report) {
            pw.printf("%s,%s,%d%n",
                    r.getSku(),
                    r.getProductName(),
                    r.getTotalQuantity()
            );
        }

        return sw.toString();
    }

    /**
     * Export daily sales summary to CSV format
     */
    public String exportDailySalesCsv() {
        List<SalesReportResponse> report = salesReportService.getDailySales();

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        pw.println("SKU,ProductName,Date,TotalQuantity");

        for (SalesReportResponse r : report) {
            pw.printf("%s,%s,%s,%d%n",
                    r.getSku(),
                    r.getProductName(),
                    r.getDate() != null ? r.getDate().toString() : "",
                    r.getTotalQuantity()
            );
        }

        return sw.toString();
    }
}
