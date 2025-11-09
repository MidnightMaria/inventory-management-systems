package com.agnesmaria.inventory.springboot.seed;

import com.agnesmaria.inventory.springboot.model.*;
import com.agnesmaria.inventory.springboot.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class SmartInventorySeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryMovementRepository movementRepository;

    private final Random random = new SecureRandom();

    private static final List<String> CITIES = Arrays.asList(
            "Jakarta", "Surabaya", "Bandung", "Medan", "Denpasar", "Semarang",
            "Makassar", "Palembang", "Balikpapan", "Yogyakarta"
    );

    private static final List<String> MOVEMENT_TYPES = Arrays.asList("IN", "OUT", "TRANSFER", "ADJUST");

    @Override
    @Transactional
    public void run(String... args) {
        if (warehouseRepository.count() > 0 && productRepository.count() > 0 && movementRepository.count() > 0) {
            System.out.println("⚠️ SmartInventorySeeder skipped — data already exists.");
            return;
        }

        System.out.println("🚀 Running SmartInventorySeeder...");

        List<Warehouse> warehouses = seedWarehouses(10);
        List<Product> products = seedProducts(200);
        seedInventoryMovements(products, warehouses, 15000);

        System.out.println("✅ SmartInventorySeeder finished — realistic inventory dataset generated.");
    }

    private List<Warehouse> seedWarehouses(int count) {
        List<Warehouse> warehouses = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String code = String.format("WH-%03d", i + 1);
            String city = CITIES.get(i % CITIES.size());

            warehouses.add(Warehouse.builder()
                    .code(code)
                    .name("Gudang " + city)
                    .address("Jl. " + city + " Raya No. " + (10 + random.nextInt(90)))
                    .latitude(-6.0 + random.nextDouble())
                    .longitude(106.0 + random.nextDouble())
                    .isActive(true)
                    .build());
        }

        List<Warehouse> saved = warehouseRepository.saveAll(warehouses);
        System.out.println("🏬 Created " + saved.size() + " warehouses.");
        return saved;
    }

    private List<Product> seedProducts(int count) {
        List<Product> products = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            String sku = String.format("PROD-%04d", i + 1);
            String name = generateProductName();
            BigDecimal price = BigDecimal.valueOf(50000 + random.nextInt(5000000));
            BigDecimal competitor = price.subtract(BigDecimal.valueOf(random.nextInt(20000)));
            int stock = 50 + random.nextInt(300);

            products.add(Product.builder()
                    .sku(sku)
                    .name(name)
                    .description("Product " + name + " for retail distribution")
                    .price(price)
                    .minStock(10)
                    .quantity(stock)
                    .dynamicPricing(random.nextBoolean())
                    .competitorPrice(competitor)
                    .build());
        }

        List<Product> saved = productRepository.saveAll(products);
        System.out.println("📦 Created " + saved.size() + " products.");
        return saved;
    }

    private void seedInventoryMovements(List<Product> products, List<Warehouse> warehouses, int count) {
        List<InventoryMovement> movements = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Product product = products.get(random.nextInt(products.size()));
            Warehouse warehouse = warehouses.get(random.nextInt(warehouses.size()));

            String type = MOVEMENT_TYPES.get(random.nextInt(MOVEMENT_TYPES.size()));
            int difference = switch (type) {
                case "IN" -> 10 + random.nextInt(50);
                case "OUT" -> -(5 + random.nextInt(30));
                case "TRANSFER" -> (random.nextBoolean() ? 1 : -1) * (5 + random.nextInt(20));
                case "ADJUST" -> random.nextInt(3) - 1;
                default -> 0;
            };

            String reason = switch (type) {
                case "IN" -> "Restock";
                case "OUT" -> "Customer Purchase";
                case "TRANSFER" -> "Warehouse Transfer";
                default -> "Inventory Adjustment";
            };

            // Simulate stock before and after
            int previousQty = product.getQuantity();
            int newQty = Math.max(0, previousQty + difference);
            product.setQuantity(newQty);

            InventoryMovement movement = InventoryMovement.builder()
                    .product(product)
                    .warehouse(warehouse)
                    .quantity(Math.abs(difference))
                    .previousQuantity(previousQty)
                    .newQuantity(newQty)
                    .difference(difference)
                    .movementType(type)
                    .reason(reason)
                    .referenceNumber("REF-" + String.format("%05d", i + 1))
                    .performedBy("System Seeder")
                    .createdAt(randomDateInLastYear())
                    .build();

            movements.add(movement);
        }

        productRepository.saveAll(products);
        movementRepository.saveAll(movements);
        System.out.println("📊 Created " + movements.size() + " inventory movement logs.");
    }

    private LocalDateTime randomDateInLastYear() {
        long start = LocalDate.of(LocalDate.now().getYear() - 1, Month.JANUARY, 1).toEpochDay();
        long end = LocalDate.now().toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(start, end);
        return LocalDate.ofEpochDay(randomDay).atTime(random.nextInt(24), random.nextInt(60));
    }

    private String generateProductName() {
        String[] baseNames = {"Laptop", "Mouse", "Keyboard", "Chair", "Fan", "Lamp", "Vacuum", "Headphones", "Shoes", "T-Shirt"};
        String[] brands = {"ASUS", "Logitech", "Nike", "IKEA", "Panasonic", "LG", "Samsung", "Lenovo", "Sony", "Adidas"};
        return brands[random.nextInt(brands.length)] + " " + baseNames[random.nextInt(baseNames.length)];
    }
}
