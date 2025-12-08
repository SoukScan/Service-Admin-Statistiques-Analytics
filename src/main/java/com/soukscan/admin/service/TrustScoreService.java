package com.soukscan.admin.service;

import com.soukscan.admin.entity.UserStats;
import com.soukscan.admin.repository.UserStatsRepository;
import org.springframework.stereotype.Service;

@Service
public class TrustScoreService {

    private final UserStatsRepository repo;

    public TrustScoreService(UserStatsRepository repo) {
        this.repo = repo;
    }

    public double computeTrustScore(Long userId) {
        UserStats stats = repo.findByUserId(userId)
                .orElse(null);

        if (stats == null) return 0.0;

        double score =
                stats.getTotalValidReports() * 2.0 +
                stats.getTotalReportsSubmitted() * 0.5 -
                stats.getWarningCount() * 1.5 -
                stats.getTotalRejectedReports() * 1.0;

        return Math.max(score, 0.0);
    }
}
