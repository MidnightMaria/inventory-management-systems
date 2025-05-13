package com.agnesmaria.inventory.springboot.service;

import com.agnesmaria.inventory.springboot.dto.SupplierRequest;
import com.agnesmaria.inventory.springboot.dto.SupplierResponse;
import com.agnesmaria.inventory.springboot.exception.SupplierAlreadyExistsException;
import com.agnesmaria.inventory.springboot.exception.SupplierNotFoundException;
import com.agnesmaria.inventory.springboot.model.Supplier;
import com.agnesmaria.inventory.springboot.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;

    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        if (supplierRepository.existsByName(request.getName())) {
            throw new SupplierAlreadyExistsException("Supplier with name '" + request.getName() + "' already exists");
        }
        if (request.getEmail() != null && supplierRepository.existsByEmail(request.getEmail())) {
            throw new SupplierAlreadyExistsException("Supplier with email '" + request.getEmail() + "' already exists");
        }
        Supplier supplier = mapToSupplier(request);
        Supplier savedSupplier = supplierRepository.save(supplier);
        return mapToSupplierResponse(savedSupplier);
    }

    @Transactional(readOnly = true)
    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(this::mapToSupplierResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found with ID: " + id));
        return mapToSupplierResponse(supplier);
    }

    @Transactional
    public SupplierResponse updateSupplier(Long id, SupplierRequest request) {
        Supplier existingSupplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException("Supplier not found with ID: " + id));

        if (!existingSupplier.getName().equalsIgnoreCase(request.getName()) && supplierRepository.existsByName(request.getName())) {
            throw new SupplierAlreadyExistsException("Supplier with name '" + request.getName() + "' already exists");
        }
        if (request.getEmail() != null && !existingSupplier.getEmail().equalsIgnoreCase(request.getEmail()) && supplierRepository.existsByEmail(request.getEmail())) {
            throw new SupplierAlreadyExistsException("Supplier with email '" + request.getEmail() + "' already exists");
        }

        Supplier updatedSupplier = mapToSupplier(request, existingSupplier);
        Supplier savedSupplier = supplierRepository.save(updatedSupplier);
        return mapToSupplierResponse(savedSupplier);
    }

    @Transactional
    public void deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new SupplierNotFoundException("Supplier not found with ID: " + id);
        }
        supplierRepository.deleteById(id);
    }

    private Supplier mapToSupplier(SupplierRequest request) {
        return Supplier.builder()
                .name(request.getName())
                .contactPerson(request.getContactPerson())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();
    }

    private Supplier mapToSupplier(SupplierRequest request, Supplier existingSupplier) {
        existingSupplier.setName(request.getName());
        existingSupplier.setContactPerson(request.getContactPerson());
        existingSupplier.setEmail(request.getEmail());
        existingSupplier.setPhone(request.getPhone());
        existingSupplier.setAddress(request.getAddress());
        return existingSupplier;
    }

    private SupplierResponse mapToSupplierResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .contactPerson(supplier.getContactPerson())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .createdAt(supplier.getCreatedAt())
                .build();
    }
}