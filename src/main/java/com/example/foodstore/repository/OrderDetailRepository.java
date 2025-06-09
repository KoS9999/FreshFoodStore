package com.example.foodstore.repository;

import com.example.foodstore.entity.Order;
import com.example.foodstore.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    List<OrderDetail> findByOrder(Order order);

    // Thống kê sản phẩm bán
    @Query(value = "SELECT p.product_name, SUM(o.quantity) AS quantity, SUM(o.quantity * o.price) AS sum, " +
            "AVG(o.price) AS avg, MIN(o.price) AS min, MAX(o.price) AS max " +
            "FROM order_details o INNER JOIN products p ON o.product_id = p.product_id " +
            "GROUP BY p.product_name", nativeQuery = true)
    List<Object[]> repo();

    // Thống kê danh mục
    @Query(value = "SELECT c.category_name, SUM(o.quantity) AS quantity, SUM(o.quantity * o.price) AS sum, " +
            "AVG(o.price) AS avg, MIN(o.price) AS min, MAX(o.price) AS max " +
            "FROM order_details o INNER JOIN products p ON o.product_id = p.product_id " +
            "INNER JOIN categories c ON p.category_id = c.category_id " +
            "GROUP BY c.category_name", nativeQuery = true)
    List<Object[]> repoWhereCategory();

    // Thống kê năm
    @Query(value = "SELECT YEAR(od.order_date), SUM(o.quantity) AS quantity, SUM(o.quantity * o.price) AS sum, " +
            "AVG(o.price) AS avg, MIN(o.price) AS min, MAX(o.price) AS max " +
            "FROM order_details o INNER JOIN orders od ON o.order_id = od.order_id " +
            "GROUP BY YEAR(od.order_date)", nativeQuery = true)
    List<Object[]> repoWhereYear();

    // Thống kê tháng
    @Query(value = "SELECT MONTH(od.order_date), SUM(o.quantity) AS quantity, SUM(o.quantity * o.price) AS sum, " +
            "AVG(o.price) AS avg, MIN(o.price) AS min, MAX(o.price) AS max " +
            "FROM order_details o INNER JOIN orders od ON o.order_id = od.order_id " +
            "GROUP BY MONTH(od.order_date)", nativeQuery = true)
    List<Object[]> repoWhereMonth();

    // Thống kê quý
    @Query(value = "SELECT QUARTER(od.order_date), SUM(o.quantity) AS quantity, SUM(o.quantity * o.price) AS sum, " +
            "AVG(o.price) AS avg, MIN(o.price) AS min, MAX(o.price) AS max " +
            "FROM order_details o INNER JOIN orders od ON o.order_id = od.order_id " +
            "GROUP BY QUARTER(od.order_date)", nativeQuery = true)
    List<Object[]> repoWhereQuarter();

    // Thống kê khách hàng
    @Query(value = "SELECT c.user_id, SUM(o.quantity) AS quantity, SUM(o.quantity * o.price) AS sum, " +
            "AVG(o.price) AS avg, MIN(o.price) AS min, MAX(o.price) AS max " +
            "FROM order_details o INNER JOIN orders p ON o.order_id = p.order_id " +
            "INNER JOIN user c ON p.user_id = c.user_id " +
            "GROUP BY c.user_id", nativeQuery = true)
    List<Object[]> reportCustomer();

    // Tỷ lệ trạng thái đơn
    @Query(value = "SELECT o.order_status, COUNT(o.order_id) AS order_count, SUM(o.amount) AS total_revenue " +
            "FROM orders o GROUP BY o.order_status", nativeQuery = true)
    List<Object[]> repoByOrderStatus();

    // Voucher đã sử dụng
    @Query(value = "SELECT o.voucher_code, COUNT(o.order_id) AS order_count, SUM(o.voucher_discount) AS total_discount, " +
            "SUM(o.amount) AS total_revenue " +
            "FROM orders o WHERE o.voucher_code IS NOT NULL GROUP BY o.voucher_code", nativeQuery = true)
    List<Object[]> repoByVoucher();

    // tỷ lệ lặp đơn
    @Query(value = "SELECT u.user_id, COUNT(DISTINCT o.order_id) AS order_count " +
            "FROM orders o INNER JOIN user u ON o.user_id = u.user_id " +
            "GROUP BY u.user_id HAVING COUNT(DISTINCT o.order_id) > 1", nativeQuery = true)
    List<Object[]> repoRepeatPurchase();

    // Sản phẩm theo mùa
    @Query(value = "SELECT p.product_name, SUM(od.quantity) AS quantity, SUM(od.quantity * od.price) AS sum " +
            "FROM order_details od INNER JOIN products p ON od.product_id = p.product_id " +
            "INNER JOIN orders o ON od.order_id = o.order_id " +
            "WHERE MONTH(o.order_date) IN (SELECT month FROM product_seasons WHERE product_id = p.product_id) " +
            "GROUP BY p.product_name", nativeQuery = true)
    List<Object[]> repoSeasonalProducts();

    // Khu vực
    @Query(value = "SELECT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(o.address, ',', 4), ',', -2)) AS region, " +
            "SUM(o.amount) AS total_revenue, COUNT(o.order_id) AS order_count " +
            "FROM orders o " +
            "WHERE o.address LIKE '%,%,%,%' " +
            "GROUP BY TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(o.address, ',', 4), ',', -2))", nativeQuery = true)
    List<Object[]> repoByRegion();
}