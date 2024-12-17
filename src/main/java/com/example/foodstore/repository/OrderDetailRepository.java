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

    // Statistics by product sold
    @Query(value = "SELECT p.product_name, SUM(o.quantity) AS quantity, SUM(o.quantity * o.price) AS sum, " +
            "AVG(o.price) AS avg, MIN(o.price) AS min, MAX(o.price) AS max " +
            "FROM order_details o INNER JOIN products p ON o.product_id = p.product_id " +
            "GROUP BY p.product_name", nativeQuery = true)
    List<Object[]> repo();

    // Statistics by category sold
    @Query(value = "SELECT c.category_name, SUM(o.quantity) AS quantity, SUM(o.quantity * o.price) AS sum, " +
            "AVG(o.price) AS avg, MIN(o.price) AS min, MAX(o.price) AS max " +
            "FROM order_details o INNER JOIN products p ON o.product_id = p.product_id " +
            "INNER JOIN categories c ON p.category_id = c.category_id " +
            "GROUP BY c.category_name", nativeQuery = true)
    List<Object[]> repoWhereCategory();

    // Statistics of products sold by year
    @Query(value = "SELECT YEAR(od.order_date), SUM(o.quantity) AS quantity, SUM(o.quantity * o.price) AS sum, " +
            "AVG(o.price) AS avg, MIN(o.price) AS min, MAX(o.price) AS max " +
            "FROM order_details o INNER JOIN orders od ON o.order_id = od.order_id " +
            "GROUP BY YEAR(od.order_date)", nativeQuery = true)
    List<Object[]> repoWhereYear();

    // Statistics of products sold by month
    @Query(value = "SELECT MONTH(od.order_date), SUM(o.quantity) AS quantity, SUM(o.quantity * o.price) AS sum, " +
            "AVG(o.price) AS avg, MIN(o.price) AS min, MAX(o.price) AS max " +
            "FROM order_details o INNER JOIN orders od ON o.order_id = od.order_id " +
            "GROUP BY MONTH(od.order_date)", nativeQuery = true)
    List<Object[]> repoWhereMonth();

    // Statistics of products sold by quarter
    @Query(value = "SELECT QUARTER(od.order_date), SUM(o.quantity) AS quantity, SUM(o.quantity * o.price) AS sum, " +
            "AVG(o.price) AS avg, MIN(o.price) AS min, MAX(o.price) AS max " +
            "FROM order_details o INNER JOIN orders od ON o.order_id = od.order_id " +
            "GROUP BY QUARTER(od.order_date)", nativeQuery = true)
    List<Object[]> repoWhereQuarter();

    // Statistics by user
    @Query(value = "SELECT c.user_id, SUM(o.quantity) AS quantity, SUM(o.quantity * o.price) AS sum, " +
            "AVG(o.price) AS avg, MIN(o.price) AS min, MAX(o.price) AS max " +
            "FROM order_details o INNER JOIN orders p ON o.order_id = p.order_id " +
            "INNER JOIN user c ON p.user_id = c.user_id " +
            "GROUP BY c.user_id", nativeQuery = true)
    List<Object[]> reportCustomer();
}

