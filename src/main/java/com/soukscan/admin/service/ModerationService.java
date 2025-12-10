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

    //  Créer un report utilisateur
    public ModerationReport createReport(ModerationReport report) {
        report.setStatus("PENDING");
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        ModerationReport saved = reportRepo.save(report);

        updateUserStatsOnReport(report.getReporterId());

        return saved;
    }

    //  Action admin sur un report (approve / reject / warn)
    public ModerationAction moderate(Long reportId, Long adminId, String actionType, String comment) {
        ModerationReport report = reportRepo.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        // Mettre à jour le report
        report.setStatus(actionType.toUpperCase());
        report.setUpdatedAt(LocalDateTime.now());
        reportRepo.save(report);

        // Créer une action modération
        ModerationAction action = new ModerationAction();
        action.setAdminId(adminId);
        action.setReportId(reportId);
        action.setActionType(actionType);
        action.setComment(comment);

        ModerationAction savedAction = actionRepo.save(action);

        // Mise à jour des stats user
        updateUserStatsOnModeration(report.getReporterId(), actionType);

        // Log admin
        logService.logAction(adminId, actionType, "REPORT", reportId, comment);

        return savedAction;
    }

    // ⬆ Stats : chaque report augmente le compteur
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

    // ⬆ Stats : mise à jour selon décision admin
    private void updateUserStatsOnModeration(Long userId, String actionType) {
        UserStats stats = userStatsRepo.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("UserStats not found"));

        switch (actionType.toUpperCase()) {
            case "APPROVE" -> stats.setTotalValidReports(stats.getTotalValidReports() + 1);
            case "REJECT" -> stats.setTotalRejectedReports(stats.getTotalRejectedReports() + 1);
            case "WARN" -> stats.setWarningCount(stats.getWarningCount() + 1);
        }

        stats.setLastActivity(LocalDateTime.now());
        userStatsRepo.save(stats);
    }

    public List<ModerationReport> getPendingReports() {
        return reportRepo.findByStatus("PENDING");
    }

    public List<ModerationReport> getReportsByUser(Long userId) {
        return reportRepo.findByReporterId(userId);
    }

    // ---- CONVENIENCE METHODS FOR CONTROLLER ----

    /**
     * Approve a moderation report.
     */
    public ModerationAction approveReport(Long reportId, Long adminId, ModerationActionDTO dto) {
        String comment = (dto != null && dto.getComment() != null) ? dto.getComment() : "Report approved";
        return moderate(reportId, adminId, "APPROVE", comment);
    }

    /**
     * Reject a moderation report.
     */
    public ModerationAction rejectReport(Long reportId, Long adminId, ModerationActionDTO dto) {
        String comment = (dto != null && dto.getComment() != null) ? dto.getComment() : "Report rejected";
        return moderate(reportId, adminId, "REJECT", comment);
    }

    /**
     * Send a warning to a user.
     */
    public ModerationAction warnUser(Long reportId, Long adminId, ModerationActionDTO dto) {
        String comment = (dto != null && dto.getComment() != null) ? dto.getComment() : "User warned";
        return moderate(reportId, adminId, "WARN", comment);
    }

    /**
     * Block a user account.
     */
    public ModerationAction blockUser(Long userId, Long adminId, ModerationActionDTO dto) {
        String comment = (dto != null && dto.getComment() != null) ? dto.getComment() : "User blocked";

        ModerationAction action = new ModerationAction();
        action.setAdminId(adminId);
        action.setReportId(userId); // Store userId as reportId for block actions
        action.setActionType("BLOCK");
        action.setComment(comment);

        ModerationAction savedAction = actionRepo.save(action);

        // Log admin action
        logService.logAction(adminId, "BLOCK", "USER", userId, comment);

        return savedAction;
    }

    /**
     * Get all moderation actions.
     */
    public List<ModerationAction> getAllActions() {
        return actionRepo.findAll();
    }
}
