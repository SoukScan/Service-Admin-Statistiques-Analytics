package com.soukscan.admin.repository;

import com.soukscan.admin.entity.AdminActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {

    List<AdminActionLog> findByAdminId(Long adminId);

    List<AdminActionLog> findByTargetType(String targetType);

    List<AdminActionLog> findByActionType(String actionType);

    List<AdminActionLog> findByTargetId(Long targetId);
}
