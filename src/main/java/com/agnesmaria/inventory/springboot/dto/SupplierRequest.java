package com.agnesmaria.inventory.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierRequest {
    @NotBlank(message = "Supplier name is required")
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
}