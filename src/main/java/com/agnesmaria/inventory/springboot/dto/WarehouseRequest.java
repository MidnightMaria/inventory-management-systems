package com.agnesmaria.inventory.springboot.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseRequest {
    @NotBlank(message = "Warehouse code is required")
    @Pattern(regexp = "^WH-[0-9]{3}$", message = "Code must follow format 'WH-XXX'")
    private String code;
    
    @NotBlank(message = "Warehouse name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;
    
    @Size(max = 200, message = "Location cannot exceed 200 characters")
    private String location;
}