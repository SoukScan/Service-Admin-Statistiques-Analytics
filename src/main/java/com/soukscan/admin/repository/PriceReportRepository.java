package com.soukscan.admin.repository;

import com.soukscan.admin.entity.PriceReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PriceReportRepository extends JpaRepository<PriceReport, Long> {

    List<PriceReport> findByReporterId(Long reporterId);

    List<PriceReport> findByProductId(Long productId);

    List<PriceReport> findByStatus(String status);
}
