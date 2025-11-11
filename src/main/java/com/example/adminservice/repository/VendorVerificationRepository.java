package com.example.adminservice.repository;

import com.example.adminservice.model.VendorVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;



public interface VendorVerificationRepository extends JpaRepository<VendorVerification, Long> {
    List<VendorVerification> findByStatus(String status);
    Optional<VendorVerification> findByVendorId(String vendorId);
}

