package com.example.foodstore.repository;

import com.example.foodstore.entity.ViewLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;

public interface ViewLogRepository extends JpaRepository<ViewLog, Long> {
    void deleteByViewTimeBefore(Date cutoffDate);

}
