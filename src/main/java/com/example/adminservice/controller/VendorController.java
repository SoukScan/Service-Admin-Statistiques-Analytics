package com.example.adminservice.controller;

import com.example.adminservice.model.VendorVerification;
import com.example.adminservice.service.VendorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/vendors")
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @GetMapping("/pending")
    public List<VendorVerification> getPendingVerifications() {
        return vendorService.getPendingVerifications();
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<String> verifyVendor(@PathVariable String id,
                                               @RequestParam(required = false) String comment,
                                               Authentication auth) {
        vendorService.verifyVendor(id, comment, auth.getName());
        return ResponseEntity.ok("Vendeur vérifié avec succès");
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<String> rejectVendor(@PathVariable String id,
                                               @RequestParam(required = false) String comment,
                                               Authentication auth) {
        vendorService.rejectVendor(id, comment, auth.getName());
        return ResponseEntity.ok("Vérification rejetée");
    }
}
