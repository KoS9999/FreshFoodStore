package com.example.foodstore.repository;

import com.example.foodstore.entity.Attendance;
import com.example.foodstore.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByUserAndDate(User user, LocalDate date);

    long countByUser(User user);

    List<Attendance> findByUserOrderByDateDesc(User user, Pageable pageable);
}
