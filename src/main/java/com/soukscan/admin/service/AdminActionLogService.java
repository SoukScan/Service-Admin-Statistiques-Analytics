package com.soukscan.admin.service;

import com.soukscan.admin.entity.AdminActionLog;
import com.soukscan.admin.repository.AdminActionLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for logging admin actions.
 * All admin operations (vendor verification, report approval, etc.) are logged here.
 */
@Service
public class AdminActionLogService {

    private final AdminActionLogRepository repository;

    public AdminActionLogService(AdminActionLogRepository repository) {
        this.repository = repository;
    }

    /**
     * Log an admin action.
     * @param adminId The ID of the admin performing the action
     * @param actionType The type of action (e.g., "VENDOR_VERIFIED", "REPORT_APPROVED")
     * @param targetType The type of target (e.g., "VENDOR", "REPORT", "USER")
     * @param targetId The ID of the target object
     * @param comment Additional comment or reason
     */
    public AdminActionLog logAction(Long adminId, String actionType, String targetType, Long targetId, String comment) {
        AdminActionLog log = new AdminActionLog();
        log.setAdminId(adminId);
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setComment(comment);

        return repository.save(log);
    }

    /**
     * Get all admin action logs.
     */
    public List<AdminActionLog> getAll() {
        return repository.findAll();
    }

    /**
     * Get all actions by a specific admin.
     */
    public List<AdminActionLog> getByAdminId(Long adminId) {
        return repository.findByAdminId(adminId);
    }

    /**
     * Get all actions of a specific type.
     */
    public List<AdminActionLog> getByActionType(String actionType) {
        return repository.findByActionType(actionType);
    }

    /**
     * Get all actions on a specific target type.
     */
    public List<AdminActionLog> getByTargetType(String targetType) {
        return repository.findByTargetType(targetType);
    }

    /**
     * Get all actions on a specific target.
     */
    public List<AdminActionLog> getByTargetId(Long targetId) {
        return repository.findByTargetId(targetId);
    }
}
