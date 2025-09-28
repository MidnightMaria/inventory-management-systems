package com.agnesmaria.inventory.springboot.service;

import com.agnesmaria.inventory.springboot.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryAlertService {

    private final ProductRepository productRepository;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 9 * * ?") // Setiap jam 9 pagi
    public void checkLowStock() {
        productRepository.findByQuantityLessThanMinStock()
            .forEach(product -> emailService.sendAlert(
                "Stok Rendah: " + product.getName(),
                "Stok tersisa: " + product.getQuantity()
            ));
    }
}