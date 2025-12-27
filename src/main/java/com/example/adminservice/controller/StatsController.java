package com.example.adminservice.controller;

import com.example.adminservice.model.Stats;
import com.example.adminservice.service.StatsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    public Stats getCurrentStats() {
        return statsService.getCurrentStats();
    }

    @GetMapping("/history")
    public List<Stats> getStatsHistory() {
        return statsService.getStatsHistory();
    }
}
