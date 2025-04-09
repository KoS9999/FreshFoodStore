package com.example.foodstore.service;

import com.example.foodstore.entity.Attendance;
import com.example.foodstore.entity.User;

import java.util.List;

public interface AttendanceService {
    boolean checkInToday();

    long countDaysCheckedIn(User user);

    List<Attendance> getRecentAttendances(User user, int limit);

}
