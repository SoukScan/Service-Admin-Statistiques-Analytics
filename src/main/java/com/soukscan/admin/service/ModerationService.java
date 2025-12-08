package com.soukscan.admin.service;

import com.soukscan.admin.dto.ModerationActionDTO;
import com.soukscan.admin.entity.ModerationAction;
import com.soukscan.admin.entity.ModerationReport;
import com.soukscan.admin.entity.UserStats;
import com.soukscan.admin.repository.ModerationActionRepository;
import com.soukscan.admin.repository.ModerationReportRepository;
import com.soukscan.admin.repository.UserStatsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for handling moderation operations.
 * Manages report creation, approval/rejection, user warnings, and account blocking.
 */
@Service
public class ModerationService {

    private final ModerationReportRepository reportRepo;
    private final ModerationActionRepository actionRepo;
    private final UserStatsRepository userStatsRepo;
    private final AdminActionLogService logService;

    public ModerationService(
            ModerationReportRepository reportRepo,
            ModerationActionRepository actionRepo,
            UserStatsRepository userStatsRepo,
            AdminActionLogService logService
    ) {
        this.reportRepo = reportRepo;
        this.actionRepo = actionRepo;
        this.userStatsRepo = userStatsRepo;
        this.logService = logService;
    }

    /**
     * Create a new moderation report.
     */
    public ModerationReport createReport(ModerationReport report) {
        report.setStatus("PENDING");
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        ModerationReport saved = reportRepo.save(report);
        updateUserStatsOnReport(report.getReporterId());

        return saved;
    }

    /**
     * Get all pending reports.
     */
    public List<ModerationReport> getPendingReports() {
        return reportRepo.findByStatus("PENDING");
    }

    /**
     * Get reports by reporter user ID.
     */
    public List<ModerationReport> getReportsByUser(Long userId) {
        return reportRepo.findByReporterId(userId);
    }

    /**
     * Get all moderation actions.
     */
    public List<ModerationAction> getAllActions() {
        return actionRepo.findAll();
    }

    /**
     * Approve a moderation report.
     */
    public ModerationAction approveReport(Long reportId, Long adminId, ModerationActionDTO dto) {
        ModerationReport report = reportRepo.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        report.setStatus("APPROVED");
        report.setUpdatedAt(LocalDateTime.now());
        reportRepo.save(report);

        String reason = (dto != null) ? dto.getComment() : "Report approved";

        ModerationAction action = new ModerationAction();
        action.setAdminId(adminId);
        action.setReportId(reportId);
        action.setActionType("APPROVE");
        action.setComment(reason);

        ModerationAction savedAction = actionRepo.save(action);

        updateUserStatsOnModeration(report.getReporterId(), "APPROVE");

        logService.logAction(
                adminId,
                "REPORT_APPROVED",
                "REPORT",
                reportId,
                reason
        );

        return savedAction;
    }

    /**
     * Reject a moderation report.
     */
    public ModerationAction rejectReport(Long reportId, Long adminId, ModerationActionDTO dto) {
        ModerationReport report = reportRepo.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        report.setStatus("REJECTED");
        report.setUpdatedAt(LocalDateTime.now());
        reportRepo.save(report);

        String reason = (dto != null) ? dto.getComment() : "Report rejected";

        ModerationAction action = new ModerationAction();
        action.setAdminId(adminId);
        action.setReportId(reportId);
        action.setActionType("REJECT");
        action.setComment(reason);

        ModerationAction savedAction = actionRepo.save(action);

        updateUserStatsOnModeration(report.getReporterId(), "REJECT");

        logService.logAction(
                adminId,
                "REPORT_REJECTED",
                "REPORT",
                reportId,
                reason
        );

        return savedAction;
    }

    /**
     * Warn a user.
     */
    public ModerationAction warnUser(Long reportId, Long adminId, ModerationActionDTO dto) {
        ModerationReport report = reportRepo.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        String comment = (dto != null) ? dto.getComment() : "User warned";

        ModerationAction action = new ModerationAction();
        action.setAdminId(adminId);
        action.setReportId(reportId);
        action.setActionType("WARN");
        action.setComment(comment);

        ModerationAction savedAction = actionRepo.save(action);

        updateUserStatsOnModeration(report.getReporterId(), "WARN");

        logService.logAction(
                adminId,
                "USER_WARNED",
                "USER",
                report.getReporterId(),
                comment
        );

        return savedAction;
    }

    /**
     * Block a user.
     */
    public ModerationAction blockUser(Long userId, Long adminId, ModerationActionDTO dto) {
        String comment = (dto != null) ? dto.getComment() : "User blocked";

        ModerationAction action = new ModerationAction();
        action.setAdminId(adminId);
        action.setActionType("BLOCK");
        action.setComment(comment);

        ModerationAction savedAction = actionRepo.save(action);

        UserStats stats = userStatsRepo.findByUserId(userId)
                .orElseGet(() -> {
                    UserStats s = new UserStats();
                    s.setUserId(userId);
                    return userStatsRepo.save(s);
                });

        stats.setLastActivity(LocalDateTime.now());
        userStatsRepo.save(stats);

        logService.logAction(
                adminId,
                "USER_BLOCKED",
                "USER",
                userId,
                comment
        );

        return savedAction;
    }

    /**
     * Internal helper: update user stats when report is created.
     */
    private void updateUserStatsOnReport(Long userId) {
        UserStats stats = userStatsRepo.findByUserId(userId)
                .orElseGet(() -> {
                    UserStats s = new UserStats();
                    s.setUserId(userId);
                    return userStatsRepo.save(s);
                });

        stats.setTotalReportsSubmitted(stats.getTotalReportsSubmitted() + 1);
        stats.setLastActivity(LocalDateTime.now());
        userStatsRepo.save(stats);
    }

    /**
     * Internal helper: update user stats based on moderation action.
     */
    private void updateUserStatsOnModeration(Long userId, String actionType) {
        UserStats stats = userStatsRepo.findByUserId(userId)
                .orElseGet(() -> {
                    UserStats s = new UserStats();
                    s.setUserId(userId);
                    return userStatsRepo.save(s);
                });

        switch (actionType.toUpperCase()) {
            case "APPROVE" -> stats.setTotalValidReports(stats.getTotalValidReports() + 1);
            case "REJECT" -> stats.setTotalRejectedReports(stats.getTotalRejectedReports() + 1);
            case "WARN" -> stats.setWarningCount(stats.getWarningCount() + 1);
        }

        stats.setLastActivity(LocalDateTime.now());
        userStatsRepo.save(stats);
    }
}
