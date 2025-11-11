package com.example.adminservice.service;

import com.example.adminservice.model.Stats;
import com.example.adminservice.repository.StatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;
import java.util.List;

@Service
public class StatsService {

    private final StatsRepository statsRepository;

    public StatsService(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    
    public Stats getCurrentStats() {
        return statsRepository.findAll()
                .stream()
                .reduce((first, second) -> second)
                .orElseGet(Stats::new);
    }

   
    public List<Stats> getStatsHistory() {
        return statsRepository.findAll();
    }

    
    @NonNull
    public Stats saveStats(@NonNull Stats stats) {
        return statsRepository.save(stats);
    }
}
