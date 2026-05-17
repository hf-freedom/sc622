package com.hotel.controller;

import com.hotel.entity.Statistics;
import com.hotel.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping
    public Statistics getStatistics() {
        return statisticsService.getStatistics();
    }

    @GetMapping("/alerts")
    public List<String> getAlerts() {
        return statisticsService.getAlerts();
    }
}
