package com.agnesmaria.inventory.springboot.controller;

import com.agnesmaria.inventory.springboot.dto.SalesRequest;
import com.agnesmaria.inventory.springboot.model.Sales;
import com.agnesmaria.inventory.springboot.service.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;

    @PostMapping
    public ResponseEntity<Sales> createSale(@RequestBody SalesRequest request) {
        Sales sale = salesService.createSale(request);
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
}
