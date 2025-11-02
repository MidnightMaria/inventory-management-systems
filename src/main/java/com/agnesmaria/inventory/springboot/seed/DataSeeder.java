package com.agnesmaria.inventory.springboot.seed;

import com.agnesmaria.inventory.springboot.model.*;
import com.agnesmaria.inventory.springboot.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepo;
    private final WarehouseRepository warehouseRepo;
    private final InventoryMovementRepository movementRepo;

    @Value("${app.seed-data:true}")
    private boolean seedDataEnabled;

    public DataSeeder(ProductRepository productRepo,
                      WarehouseRepository warehouseRepo,
                      InventoryMovementRepository movementRepo) {
        this.productRepo = productRepo;
        this.warehouseRepo = warehouseRepo;
        this.movementRepo = movementRepo;
    }

    @Override
    public void run(String... args) {
        if (!seedDataEnabled) {
            System.out.println("⚠️ Inventory seeding skipped (app.seed-data=false)");
            return;
        }

        seedWarehouses();
        seedProducts();
        seedMovements();
    }

    private void seedWarehouses() {
        if (warehouseRepo.count() == 0) {
            List<Warehouse> warehouses = List.of(
                    Warehouse.builder().code("WH-001").name("Gudang Bandung")
                            .address("Jl. Asia Afrika No. 123")
                            .latitude(-6.92).longitude(107.61).isActive(true).build(),
                    Warehouse.builder().code("WH-002").name("Gudang Jakarta")
                            .address("Jl. Gatot Subroto No. 45")
                            .latitude(-6.21).longitude(106.82).isActive(true).build(),
                    Warehouse.builder().code("WH-003").name("Gudang Surabaya")
                            .address("Jl. Pahlawan No. 99")
                            .latitude(-7.25).longitude(112.75).isActive(true).build()
            );
            warehouseRepo.saveAll(warehouses);
            System.out.println("🏗️ Warehouses seeded (" + warehouses.size() + ")");
        }
    }

    private void seedProducts() {
        if (productRepo.count() == 0) {
            List<Product> products = List.of(
                    Product.builder().sku("PROD-001").name("Laptop ASUS ROG")
                            .description("High performance gaming laptop")
                            .price(BigDecimal.valueOf(20000000))
                            .quantity(10).minStock(2)
                            .dynamicPricing(false).competitorPrice(BigDecimal.valueOf(19500000)).build(),
                    Product.builder().sku("PROD-002").name("Monitor LG UltraGear 27\"")
                            .description("165Hz QHD gaming monitor")
                            .price(BigDecimal.valueOf(4500000))
                            .quantity(20).minStock(3)
                            .dynamicPricing(false).competitorPrice(BigDecimal.valueOf(4400000)).build(),
                    Product.builder().sku("PROD-003").name("Keyboard Logitech G Pro X")
                            .description("Mechanical keyboard hot-swap switches")
                            .price(BigDecimal.valueOf(1800000))
                            .quantity(50).minStock(5)
                            .dynamicPricing(true).competitorPrice(BigDecimal.valueOf(1750000)).build(),
                    Product.builder().sku("PROD-004").name("Mouse Razer Viper Mini")
                            .description("Ultra lightweight gaming mouse")
                            .price(BigDecimal.valueOf(800000))
                            .quantity(70).minStock(10)
                            .dynamicPricing(true).competitorPrice(BigDecimal.valueOf(750000)).build(),
                    Product.builder().sku("PROD-005").name("Headset HyperX Cloud II")
                            .description("7.1 surround sound gaming headset")
                            .price(BigDecimal.valueOf(1500000))
                            .quantity(30).minStock(5)
                            .dynamicPricing(false).competitorPrice(BigDecimal.valueOf(1400000)).build()
            );
            productRepo.saveAll(products);
            System.out.println("💾 Products seeded (" + products.size() + ")");
        }
    }

    private void seedMovements() {
        if (movementRepo.count() > 0) {
            System.out.println("ℹ️ Movements already exist, skipping seed.");
            return;
        }

        Random random = new Random();
        List<Product> products = productRepo.findAll();
        List<Warehouse> warehouses = warehouseRepo.findAll();
        List<String> movementTypes = List.of("IN", "OUT", "TRANSFER");

        List<InventoryMovement> movements = new ArrayList<>();
        int totalRecords = 500; // 💡 500 semi-realistic transactions
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < totalRecords; i++) {
            Product product = products.get(random.nextInt(products.size()));
            Warehouse warehouse = warehouses.get(random.nextInt(warehouses.size()));
            String movementType;

            // Simulate seasonal pattern
            LocalDateTime date = now.minusDays(random.nextInt(180));
            DayOfWeek day = date.getDayOfWeek();
            boolean isWeekend = (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY);

            // Weekends have more sales (OUT)
            if (isWeekend && random.nextDouble() < 0.7) {
                movementType = "OUT";
            } else if (i % 20 == 0) {
                movementType = "IN"; // Restock every 20 events
            } else if (i % 50 == 0) {
                movementType = "TRANSFER"; // Occasional transfer
            } else {
                movementType = movementTypes.get(random.nextInt(movementTypes.size()));
            }

            int diff = switch (movementType) {
                case "IN" -> random.nextInt(10) + 5; // restock 5–15
                case "OUT" -> -(isWeekend ? random.nextInt(10) + 3 : random.nextInt(5) + 1); // more out on weekend
                case "TRANSFER" -> random.nextInt(3) + 1;
                default -> 0;
            };

            // Simulate price fluctuation (±5%) for dynamic products
            if (product.isDynamicPricing() && random.nextDouble() < 0.15) {
                BigDecimal fluctuation = product.getPrice()
                        .multiply(BigDecimal.valueOf(0.95 + random.nextDouble() * 0.1));
                product.setPrice(fluctuation);
                productRepo.save(product);
            }

            movements.add(
                    InventoryMovement.builder()
                            .product(product)
                            .warehouse(warehouse)
                            .movementType(movementType)
                            .difference(diff)
                            .reason(generateReason(movementType))
                            .referenceNumber("REF-" + (1000 + i))
                            .performedBy("System Seeder")
                            .createdAt(date)
                            .build()
            );
        }

        movementRepo.saveAll(movements);
        System.out.println("📦 Seeded " + movements.size() + " semi-realistic inventory movements");
        System.out.println("✅ Inventory seeding complete (ready for data science)");
    }

    private String generateReason(String type) {
        return switch (type) {
            case "IN" -> "Supplier Restock";
            case "OUT" -> "Customer Purchase";
            case "TRANSFER" -> "Internal Transfer";
            default -> "Adjustment";
        };
    }
}
