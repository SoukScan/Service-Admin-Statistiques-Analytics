package com.example.adminservice.service;

import com.example.adminservice.model.DocumentInfo;
import com.example.adminservice.model.VendorVerification;
import com.example.adminservice.repository.VendorVerificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Service
public class DocumentService {

    private final VendorVerificationRepository vendorRepository;

    // 🔹 Constructeur explicite
    public DocumentService(VendorVerificationRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    // 🔹 Upload d’un document
    public void uploadDocument(String vendorId, MultipartFile file) {
        try {
            File uploadDir = new File("uploads");
            if (!uploadDir.exists()) uploadDir.mkdirs();

            String filePath = "uploads/" + file.getOriginalFilename();
            file.transferTo(new File(filePath));

            Optional<VendorVerification> vendorOpt = vendorRepository.findByVendorId(vendorId);
            if (vendorOpt.isEmpty()) {
                throw new RuntimeException("Vendeur non trouvé");
            }

            VendorVerification vendor = vendorOpt.get();
            DocumentInfo doc = new DocumentInfo();
            doc.setName(file.getOriginalFilename());
            doc.setUrl(filePath);
            doc.setVendor(vendor);

            vendor.getDocuments().add(doc);
            vendorRepository.save(vendor);

        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du téléversement : " + e.getMessage());
        }
    }

    // 🔹 Récupération des documents d’un vendeur
    public VendorVerification getDocuments(String vendorId) {
        return vendorRepository.findByVendorId(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendeur non trouvé"));
    }
}
