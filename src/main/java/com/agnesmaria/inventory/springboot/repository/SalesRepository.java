package com.agnesmaria.inventory.springboot.repository;

import com.agnesmaria.inventory.springboot.model.Product;
import com.agnesmaria.inventory.springboot.model.Sales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SalesRepository extends JpaRepository<Sales, Long> {

    // Cari semua sales berdasarkan produk
    List<Sales> findByProduct(Product product);

    // Total penjualan per produk (sku, totalQty)
    @Query("SELECT s.product.sku, SUM(s.quantity) " +
           "FROM Sales s GROUP BY s.product.sku")
    List<Object[]> getTotalSalesPerProduct();

    // Total penjualan harian (sku, totalQty, tanggal)
    @Query("SELECT s.product.sku, SUM(s.quantity), DATE(s.timestamp) " +
           "FROM Sales s GROUP BY s.product.sku, DATE(s.timestamp) " +
           "ORDER BY DATE(s.timestamp)")
    List<Object[]> getDailySales();
}
