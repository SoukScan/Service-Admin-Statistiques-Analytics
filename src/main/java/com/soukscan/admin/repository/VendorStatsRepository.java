package com.soukscan.admin.repository;

import com.soukscan.admin.entity.VendorStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VendorStatsRepository extends JpaRepository<VendorStats, Long> {

    Optional<VendorStats> findByVendorId(Long vendorId);

    boolean existsByVendorId(Long vendorId);
}
