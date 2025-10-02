package com.agnesmaria.inventory.springboot.repository;

import com.agnesmaria.inventory.springboot.model.Product;
import com.agnesmaria.inventory.springboot.model.Sales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface SalesRepository extends JpaRepository<Sales, Long> {

    // Cari semua sales berdasarkan produk
    List<Sales> findByProduct(Product product);

    // Total penjualan per produk (sku, totalQty)
    @Query("SELECT s.product.sku, s.product.name, SUM(s.quantity), SUM(s.quantity * s.price) " +
       "FROM Sales s GROUP BY s.product.sku, s.product.name")
List<Object[]> getTotalSalesPerProduct();


    // Total penjualan harian (sku, totalQty, tanggal)
    @Query("SELECT s.product.sku, SUM(s.quantity), DATE(s.timestamp) " +
           "FROM Sales s GROUP BY s.product.sku, DATE(s.timestamp) " +
           "ORDER BY DATE(s.timestamp)")
    List<Object[]> getDailySales();

    // Ambil semua sales history
    @Query("SELECT s FROM Sales s JOIN FETCH s.product ORDER BY s.timestamp DESC")
    List<Sales> findAllWithProduct();

    // Ambil sales history dengan filter tanggal
    @Query("SELECT s FROM Sales s JOIN FETCH s.product " +
           "WHERE s.timestamp BETWEEN :start AND :end ORDER BY s.timestamp ASC")
    List<Sales> findByDateRange(LocalDateTime start, LocalDateTime end);

    // Ambil sales history dengan filter SKU
    @Query("SELECT s FROM Sales s JOIN FETCH s.product " +
           "WHERE s.product.sku = :sku ORDER BY s.timestamp ASC")
    List<Sales> findBySku(String sku);

    // Kombinasi filter SKU + tanggal
    @Query("SELECT s FROM Sales s JOIN FETCH s.product " +
           "WHERE s.product.sku = :sku AND s.timestamp BETWEEN :start AND :end " +
           "ORDER BY s.timestamp ASC")
    List<Sales> findBySkuAndDateRange(String sku, LocalDateTime start, LocalDateTime end);
}
