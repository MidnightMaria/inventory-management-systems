package com.agnesmaria.inventory.springboot.service;

import com.agnesmaria.inventory.springboot.dto.WarehouseRequest;
import com.agnesmaria.inventory.springboot.dto.WarehouseResponse;
import com.agnesmaria.inventory.springboot.exception.WarehouseException;
import com.agnesmaria.inventory.springboot.model.Warehouse;
import com.agnesmaria.inventory.springboot.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @Transactional
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        validateWarehouseCode(request.getCode());
        
        Warehouse warehouse = Warehouse.builder()
                .code(request.getCode())
                .name(request.getName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isActive(request.getIsActive())
                .createdAt(LocalDateTime.now())
                .build();

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
                .orElseThrow(() -> new WarehouseException("Warehouse not found with id: " + id));
        return mapToWarehouseResponse(warehouse);
    }

    @Transactional
    public WarehouseResponse updateWarehouse(Long id, WarehouseRequest request) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseException("Warehouse not found with id: " + id));

        if (!warehouse.getCode().equals(request.getCode())) {
            validateWarehouseCode(request.getCode());
        }

        warehouse.setName(request.getName());
        warehouse.setAddress(request.getAddress());
        warehouse.setLatitude(request.getLatitude());
        warehouse.setLongitude(request.getLongitude());
        warehouse.setIsActive(request.getIsActive());
        warehouse.setUpdatedAt(LocalDateTime.now());

        Warehouse updatedWarehouse = warehouseRepository.save(warehouse);
        return mapToWarehouseResponse(updatedWarehouse);
    }

    @Transactional
    public void toggleWarehouseStatus(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseException("Warehouse not found with id: " + id));
        warehouse.setIsActive(!warehouse.getIsActive());
        warehouseRepository.save(warehouse);
    }

    private void validateWarehouseCode(String code) {
        if (warehouseRepository.existsByCode(code)) {
            throw new WarehouseException("Warehouse code already exists: " + code);
        }
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