package com.agnesmaria.inventory.springboot.service;

import com.agnesmaria.inventory.springboot.model.Warehouse;
import com.agnesmaria.inventory.springboot.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;

    @Transactional(readOnly = true)
    public Warehouse getWarehouseById(Long id) {
        return warehouseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Warehouse not found"));
    }
}