package com.example.foodstore.serviceImpl;

import com.example.foodstore.entity.Attendance;
import com.example.foodstore.entity.User;
import com.example.foodstore.repository.AttendanceRepository;
import com.example.foodstore.repository.UserRepository;
import com.example.foodstore.service.AttendanceService;
import com.example.foodstore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private static final int DAILY_POINTS = 1000;

    @Override
    public boolean checkInToday() {
        User user = userService.getLoggedInUser();
        LocalDate today = LocalDate.now();

        boolean alreadyCheckedIn = attendanceRepository.findByUserAndDate(user, today).isPresent();
        if (alreadyCheckedIn) {
            return false;
        }

        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setDate(today);
        attendance.setEarnedPoints(DAILY_POINTS);
        attendanceRepository.save(attendance);

        int earnedToday = DAILY_POINTS;

        long currentTotal = attendanceRepository.countByUser(user);
        if (currentTotal % 7 == 0) {
            earnedToday += 1000;
        }

        user.setPoints(user.getPoints() + earnedToday);
        userRepository.save(user);

        return true;
    }


    @Override
    public long countDaysCheckedIn(User user) {
        return attendanceRepository.countByUser(user);
    }

    @Override
    public List<Attendance> getRecentAttendances(User user, int limit) {
        return attendanceRepository.findByUserOrderByDateDesc(user, PageRequest.of(0, limit));
    }
}



