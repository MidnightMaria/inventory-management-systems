package com.agnesmaria.inventory.springboot.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {
    
    public void sendAlert(String subject, String message) {
        // Versi testing - hanya log ke console
        System.out.println("=== EMAIL NOTIFICATION ===");
        System.out.println("To: admin@example.com");
        System.out.println("Subject: " + subject);
        System.out.println("Message: " + message);
        System.out.println("========================");
        
        // Untuk versi production, uncomment kode berikut:
        /*
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo("admin@example.com");
        email.setSubject(subject);
        email.setText(message);
        mailSender.send(email);
        */
    }
}