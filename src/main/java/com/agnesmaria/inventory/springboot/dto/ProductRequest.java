package com.agnesmaria.inventory.springboot.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank
    @Schema(description = "SKU unik produk", example = "PROD-001")
    private String sku;
    
    @NotBlank
    @Schema(description = "Nama produk", example = "Laptop ASUS ROG")
    private String name;
    
    @Schema(description = "Deskripsi produk", example = "Laptop gaming dengan GPU RTX 3060")
    private String description;
    
    @NotNull
    @Positive
    @Schema(description = "Harga produk", example = "15000000")
    private BigDecimal price;

    @Min(0)
    @Schema(description = "Jumlah stok awal", example = "10")
    private int quantity;
    
    @Min(0)
    @Schema(description = "Jumlah minimum stok sebelum alert", example = "2")
    private int minStock;
    
    @Schema(description = "Apakah harga mengikuti dynamic pricing", example = "true")
    private boolean dynamicPricing;
    
    @Positive
    @Schema(description = "Harga kompetitor untuk dynamic pricing", example = "14800000")
    private BigDecimal competitorPrice;
}
