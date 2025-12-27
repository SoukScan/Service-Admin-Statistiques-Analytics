package com.example.adminservice.controller;

import com.example.adminservice.model.VendorVerification;
import com.example.adminservice.service.DocumentService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/{vendorId}/upload")
    public ResponseEntity<String> uploadDocument(@PathVariable String vendorId,
                                                 @RequestParam("file") MultipartFile file) {
        documentService.uploadDocument(vendorId, file);
        return ResponseEntity.ok("Document uploadé avec succès");
    }

    @GetMapping("/{vendorId}")
    public VendorVerification getDocuments(@PathVariable String vendorId) {
        return documentService.getDocuments(vendorId);
    }
}
