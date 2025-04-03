package com.example.foodstore.serviceImpl;

import com.example.foodstore.repository.ViewLogRepository;
import com.example.foodstore.service.ViewLogService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

@Service
public class ViewLogServiceImpl implements ViewLogService{
    private final ViewLogRepository viewLogRepository;

    public ViewLogServiceImpl(ViewLogRepository viewLogRepository) {
        this.viewLogRepository = viewLogRepository;
    }

    @Override
    @Scheduled(cron = "0 0 1 * * ?")
    public void cleanOldViewLogs() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -30);
        Date cutoffDate = cal.getTime();
        viewLogRepository.deleteByViewTimeBefore(cutoffDate);
    }
}
