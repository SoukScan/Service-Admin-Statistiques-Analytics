package com.soukscan.admin.service;

import com.soukscan.admin.dto.VendorDocumentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;

import java.util.Optional;

/**
 * Service for managing vendor verification documents via Vendor-Service.
 * Handles document retrieval, verification checks, and downloads.
 * Documents are NOT stored locally; they remain in Vendor-Service storage.
 */
@Service
public class VendorDocumentService {

    private static final Logger logger = LoggerFactory.getLogger(VendorDocumentService.class);

    private final WebClient vendorWebClient;

    public VendorDocumentService(@Qualifier("vendorWebClient") WebClient vendorWebClient) {
        this.vendorWebClient = vendorWebClient;
    }

    /**
     * Check if a verification document exists for a vendor.
     * Calls Vendor-Service: GET /vendors/{vendorId}/document/metadata
     *
     * @param vendorId The vendor ID
     * @return Optional containing VendorDocumentDTO if document exists
     */
    public Optional<VendorDocumentDTO> getDocumentMetadata(Long vendorId) {
        logger.debug("Fetching document metadata for vendor {}", vendorId);

        try {
            VendorDocumentDTO dto = vendorWebClient.get()
                    .uri("/{id}/document/metadata", vendorId)
                    .retrieve()
                    .onStatus(
                            status -> status.equals(HttpStatus.NOT_FOUND),
                            response -> Mono.error(new DocumentNotFoundException(
                                    "No verification document found for vendor " + vendorId
                            ))
                    )
                    .bodyToMono(VendorDocumentDTO.class)
                    .doOnError(ex -> logger.error("Error fetching document metadata for vendor {}: {}", vendorId, ex.getMessage()))
                    .onErrorResume(DocumentNotFoundException.class, ex -> Mono.empty())
                    .block();

            return Optional.ofNullable(dto);
        } catch (Exception ex) {
            logger.error("Unexpected error fetching document metadata for vendor {}: {}", vendorId, ex.getMessage());
            throw new VendorDocumentException("Failed to fetch document metadata: " + ex.getMessage());
        }
    }

    /**
     * Get the document download stream/URL from Vendor-Service.
     * Calls Vendor-Service: GET /vendors/{vendorId}/document
     *
     * @param vendorId The vendor ID
     * @return ResponseEntity containing the document or signed URL
     */
    public ResponseEntity<Object> getDocument(Long vendorId) {
        logger.debug("Fetching document for vendor {}", vendorId);

        try {
            ResponseEntity<Object> response = vendorWebClient.get()
                    .uri("/{id}/document", vendorId)
                    .retrieve()
                    .onStatus(
                            status -> status.equals(HttpStatus.NOT_FOUND),
                            resp -> Mono.error(new DocumentNotFoundException(
                                    "Document not found for vendor " + vendorId
                            ))
                    )
                    .toEntity(Object.class)
                    .doOnError(ex -> logger.error("Error fetching document for vendor {}: {}", vendorId, ex.getMessage()))
                    .block();

            logger.info("Document retrieved successfully for vendor {}", vendorId);
            return response;
        } catch (DocumentNotFoundException ex) {
            logger.warn("Document not found for vendor {}", vendorId);
            throw ex;
        } catch (WebClientException ex) {
            logger.error("Vendor-Service error fetching document for vendor {}: {}", vendorId, ex.getMessage());
            throw new VendorDocumentException("Vendor-Service error: " + ex.getMessage());
        } catch (Exception ex) {
            logger.error("Unexpected error fetching document for vendor {}: {}", vendorId, ex.getMessage());
            throw new VendorDocumentException("Failed to fetch document: " + ex.getMessage());
        }
    }

    /**
     * Verify that a document exists before approving/rejecting a vendor.
     * @param vendorId The vendor ID
     * @return true if document exists, false otherwise
     */
    public boolean hasVerificationDocument(Long vendorId) {
        logger.debug("Checking if vendor {} has a verification document", vendorId);
        return getDocumentMetadata(vendorId).isPresent();
    }

    /**
     * Get document metadata with exception handling for approval/rejection flow.
     * @param vendorId The vendor ID
     * @return VendorDocumentDTO if found
     * @throws DocumentNotFoundException if document does not exist
     */
    public VendorDocumentDTO getDocumentOrThrow(Long vendorId) throws DocumentNotFoundException {
        return getDocumentMetadata(vendorId)
                .orElseThrow(() -> new DocumentNotFoundException(
                        "Verification document is required but not found for vendor " + vendorId
                ));
    }

    /**
     * Custom exception for document-related errors.
     */
    public static class DocumentNotFoundException extends RuntimeException {
        public DocumentNotFoundException(String message) {
            super(message);
        }

        public DocumentNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Custom exception for vendor document service errors.
     */
    public static class VendorDocumentException extends RuntimeException {
        public VendorDocumentException(String message) {
            super(message);
        }

        public VendorDocumentException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
