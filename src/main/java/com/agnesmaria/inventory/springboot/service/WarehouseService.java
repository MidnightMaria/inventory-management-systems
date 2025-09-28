package com.agnesmaria.inventory.springboot.service;

import com.agnesmaria.inventory.springboot.dto.WarehouseRequest;
import com.agnesmaria.inventory.springboot.dto.WarehouseResponse;
import com.agnesmaria.inventory.springboot.exception.*;
import com.agnesmaria.inventory.springboot.model.Warehouse;
import com.agnesmaria.inventory.springboot.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @Transactional
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        validateWarehouseCodeNotExists(request.getCode());
        Warehouse warehouse = mapToWarehouse(request);
        Warehouse savedWarehouse = warehouseRepository.save(warehouse);
        return mapToWarehouseResponse(savedWarehouse);
    }

    public List<WarehouseResponse> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(this::mapToWarehouseResponse)
                .collect(Collectors.toList());
    }

    public WarehouseResponse getWarehouseById(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException(id));
        return mapToWarehouseResponse(warehouse);
    }

    @Transactional
    public WarehouseResponse updateWarehouse(Long id, WarehouseRequest request) {
        Warehouse existingWarehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException(id));
        if (!existingWarehouse.getCode().equals(request.getCode())) {
            validateWarehouseCodeNotExists(request.getCode());
        }
        Warehouse updatedWarehouse = mapToWarehouse(request, existingWarehouse);
        Warehouse savedWarehouse = warehouseRepository.save(updatedWarehouse);
        return mapToWarehouseResponse(savedWarehouse);
    }

    @Transactional
    public void toggleWarehouseStatus(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException(id));
        warehouse.setIsActive(!warehouse.getIsActive());
        warehouseRepository.save(warehouse);
    }

    public List<WarehouseResponse> getAllActiveWarehouses() {
        return warehouseRepository.findByIsActiveTrue().stream()
                .map(this::mapToWarehouseResponse)
                .collect(Collectors.toList());
    }

    private void validateWarehouseCodeNotExists(String code) {
        if (warehouseRepository.existsByCode(code)) {
            throw new WarehouseCodeAlreadyExistsException(code);
        }
    }

    private Warehouse mapToWarehouse(WarehouseRequest request) {
        return Warehouse.builder()
                .code(request.getCode())
                .name(request.getName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isActive(request.getIsActive())
                .build();
    }

    private Warehouse mapToWarehouse(WarehouseRequest request, Warehouse existingWarehouse) {
        existingWarehouse.setCode(request.getCode());
        existingWarehouse.setName(request.getName());
        existingWarehouse.setAddress(request.getAddress());
        existingWarehouse.setLatitude(request.getLatitude());
        existingWarehouse.setLongitude(request.getLongitude());
        existingWarehouse.setIsActive(request.getIsActive());
        return existingWarehouse;
    }

    private WarehouseResponse mapToWarehouseResponse(Warehouse warehouse) {
        return WarehouseResponse.builder()
                .id(warehouse.getId())
                .code(warehouse.getCode())
                .name(warehouse.getName())
                .address(warehouse.getAddress())
                .latitude(warehouse.getLatitude())
                .longitude(warehouse.getLongitude())
                .isActive(warehouse.getIsActive())
                .createdAt(warehouse.getCreatedAt())
                .updatedAt(warehouse.getUpdatedAt())
                .build();
    }
}