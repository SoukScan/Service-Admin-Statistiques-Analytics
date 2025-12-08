package com.soukscan.admin.service;

import com.soukscan.admin.entity.PriceReport;
import com.soukscan.admin.repository.PriceReportRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PriceReportService {

    private final PriceReportRepository repo;

    public PriceReportService(PriceReportRepository repo) {
        this.repo = repo;
    }

    public List<PriceReport> getAll() {
        return repo.findAll();
    }

    public List<PriceReport> getByStatus(String status) {
        return repo.findByStatus(status);
    }

    public PriceReport updateStatus(Long id, String newStatus) {
        PriceReport r = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        r.setStatus(newStatus);
        repo.save(r);

        return r;
    }
}
