package com.agnesmaria.inventory.springboot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {
    private String username; // <--- GANTI EMAIL DENGAN USERNAME
    private String password;
}