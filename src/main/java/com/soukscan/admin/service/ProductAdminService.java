package com.soukscan.admin.service;

import com.soukscan.admin.dto.ProductDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Service for managing products via the Product microservice.
 * Provides CRUD and search operations for products.
 */
@Service
public class ProductAdminService {

    private final WebClient productWebClient;
    private final AdminActionLogService adminActionLogService;

        public ProductAdminService(@Qualifier("productWebClient") WebClient productWebClient,
                                                           AdminActionLogService adminActionLogService) {
                this.productWebClient = productWebClient;
                this.adminActionLogService = adminActionLogService;
        }

    /**
     * Get all products from the Product service.
     */
    public List<ProductDTO> getAllProducts() {
        ProductDTO[] products = productWebClient.get()
                .uri("")
                .retrieve()
                .bodyToMono(ProductDTO[].class)
                .onErrorResume(ex -> Mono.error(new RuntimeException(
                        "Product-Service error fetching all products: " + ex.getMessage()
                )))
                .block();

        return products != null ? Arrays.asList(products) : List.of();
    }

    /**
     * Get a specific product by ID.
     */
    public ProductDTO getProduct(Long productId) {
        return productWebClient.get()
                .uri("/{id}", productId)
                .retrieve()
                .bodyToMono(ProductDTO.class)
                .onErrorResume(ex -> Mono.error(new RuntimeException(
                        "Product-Service error fetching product " + productId + ": " + ex.getMessage()
                )))
                .block();
    }

    /**
     * Create a new product.
     */
    public ProductDTO createProduct(ProductDTO dto, Long adminId) {
        ProductDTO created = productWebClient.post()
                .uri("")
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(ProductDTO.class)
                .onErrorResume(ex -> Mono.error(new RuntimeException(
                        "Product-Service error creating product: " + ex.getMessage()
                )))
                .block();

        if (created != null) {
            adminActionLogService.logAction(
                    adminId,
                    "PRODUCT_CREATED",
                    "PRODUCT",
                    created.getId(),
                    "Product created: " + created.getName()
            );
        }

        return created;
    }

    /**
     * Update an existing product.
     */
    public ProductDTO updateProduct(Long productId, ProductDTO dto, Long adminId) {
        ProductDTO updated = productWebClient.put()
                .uri("/{id}", productId)
                .bodyValue(dto)
                .retrieve()
                .bodyToMono(ProductDTO.class)
                .onErrorResume(ex -> Mono.error(new RuntimeException(
                        "Product-Service error updating product " + productId + ": " + ex.getMessage()
                )))
                .block();

        if (updated != null) {
            adminActionLogService.logAction(
                    adminId,
                    "PRODUCT_UPDATED",
                    "PRODUCT",
                    productId,
                    "Product updated: " + updated.getName()
            );
        }

        return updated;
    }

    /**
     * Delete a product.
     */
    public void deleteProduct(Long productId, Long adminId) {
        productWebClient.delete()
                .uri("/{id}", productId)
                .retrieve()
                .toBodilessEntity()
                .onErrorResume(ex -> Mono.error(new RuntimeException(
                        "Product-Service error deleting product " + productId + ": " + ex.getMessage()
                )))
                .block();

        adminActionLogService.logAction(
                adminId,
                "PRODUCT_DELETED",
                "PRODUCT",
                productId,
                "Product deleted"
        );
    }

    /**
     * Search products by name.
     */
    public List<ProductDTO> searchByName(String name) {
        ProductDTO[] products = productWebClient.get()
                .uri("/search?name={name}", name)
                .retrieve()
                .bodyToMono(ProductDTO[].class)
                .onErrorResume(ex -> Mono.error(new RuntimeException(
                        "Product-Service error searching products: " + ex.getMessage()
                )))
                .block();

        return products != null ? Arrays.asList(products) : List.of();
    }

    /**
     * Find products by category.
     */
    public List<ProductDTO> findByCategory(String category) {
        ProductDTO[] products = productWebClient.get()
                .uri("/category/{category}", category)
                .retrieve()
                .bodyToMono(ProductDTO[].class)
                .onErrorResume(ex -> Mono.error(new RuntimeException(
                        "Product-Service error finding products by category: " + ex.getMessage()
                )))
                .block();

        return products != null ? Arrays.asList(products) : List.of();
    }

    /**
     * Get product suggestions.
     */
    public List<ProductDTO> getSuggestions(String query) {
        ProductDTO[] products = productWebClient.get()
                .uri("/suggestions?query={query}", query)
                .retrieve()
                .bodyToMono(ProductDTO[].class)
                .onErrorResume(ex -> Mono.error(new RuntimeException(
                        "Product-Service error getting suggestions: " + ex.getMessage()
                )))
                .block();

        return products != null ? Arrays.asList(products) : List.of();
    }
}
