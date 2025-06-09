package com.example.foodstore.repository;

import com.example.foodstore.entity.ViewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface ViewLogRepository extends JpaRepository<ViewLog, Long> {
    void deleteByViewTimeBefore(Date cutoffDate);
    @Query("SELECT vl FROM ViewLog vl JOIN FETCH vl.product WHERE vl.user.userId = :userId ORDER BY vl.viewTime DESC")
    List<ViewLog> findTop11ByUserUserIdOrderByViewTimeDesc(@Param("userId") Long userId);}
