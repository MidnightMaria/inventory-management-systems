package com.agnesmaria.inventory.springboot.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseRequest {
    @NotBlank(message = "Warehouse code is required")
    @Pattern(regexp = "^WH-[A-Z0-9]{3,8}$", message = "Code must follow format 'WH-XXX' (3-8 alphanumeric chars)")
    private String code;
    
    @NotBlank(message = "Warehouse name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;
    
    @Size(max = 500, message = "Address cannot exceed 500 characters")
    private String address;
    
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;
    
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;
    
    @NotNull(message = "Active status must be specified")
    private Boolean isActive;
}