package com.example.adminservice.service;

import com.example.adminservice.model.VendorVerification;
import com.example.adminservice.repository.VendorVerificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VendorService {

    private final VendorVerificationRepository repo;

    public VendorService(VendorVerificationRepository repo) {
        this.repo = repo;
    }

    public List<VendorVerification> getPendingVerifications() {
        return repo.findByStatus("pending");
    }

    public void verifyVendor(String vendorId, String comment, String admin) {
        VendorVerification v = repo.findByVendorId(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendeur non trouvé"));
        v.setStatus("verified");
        v.setAdminComment(comment);
        v.setVerifiedBy(admin);
        v.setUpdatedAt(LocalDateTime.now());
        repo.save(v);
    }

    public void rejectVendor(String vendorId, String comment, String admin) {
        VendorVerification v = repo.findByVendorId(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendeur non trouvé"));
        v.setStatus("rejected");
        v.setAdminComment(comment);
        v.setVerifiedBy(admin);
        v.setUpdatedAt(LocalDateTime.now());
        repo.save(v);
    }
}
