package com.soukscan.admin.repository;

import com.soukscan.admin.entity.ModerationReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModerationReportRepository extends JpaRepository<ModerationReport, Long> {

    List<ModerationReport> findByReporterId(Long reporterId);

    List<ModerationReport> findByTargetType(String targetType);

    List<ModerationReport> findByTargetId(Long targetId);

    List<ModerationReport> findByStatus(String status);
}
