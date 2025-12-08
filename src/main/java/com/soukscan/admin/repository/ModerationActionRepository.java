package com.soukscan.admin.repository;

import com.soukscan.admin.entity.ModerationAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModerationActionRepository extends JpaRepository<ModerationAction, Long> {

    List<ModerationAction> findByAdminId(Long adminId);

    List<ModerationAction> findByReportId(Long reportId);

    List<ModerationAction> findByActionType(String actionType);
}
