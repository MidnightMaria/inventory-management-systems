package com.agnesmaria.inventory.springboot.service;

import com.agnesmaria.inventory.springboot.model.Sales;
import com.agnesmaria.inventory.springboot.repository.SalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesService {

    private final SalesRepository salesRepository;

    public Sales createSale(String sku, int quantity) {
        Sales sales = Sales.builder()
                .sku(sku)
                .quantity(quantity)
                .timestamp(LocalDateTime.now())
                .build();
        return salesRepository.save(sales);
    }

    public List<Sales> getAllSales() {
        return salesRepository.findAll();
    }

    public List<Sales> getSalesBySku(String sku) {
        return salesRepository.findBySku(sku);
    }
}
