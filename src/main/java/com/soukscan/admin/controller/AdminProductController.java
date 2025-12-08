package com.soukscan.admin.controller;

import com.soukscan.admin.dto.ProductDTO;
import com.soukscan.admin.service.ProductAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for administering products via the Product microservice.
 * Allows admins to create, update, delete, and search products.
 *
 * All endpoints require JWT Bearer token authentication with ROLE_ADMIN or ROLE_MODERATOR.
 */
@RestController
@RequestMapping("/admin/products")
@Tag(name = "Admin - Products", description = "Product management endpoints for admins")
@SecurityRequirement(name = "bearerAuth")
public class AdminProductController {

    private final ProductAdminService productAdminService;

    public AdminProductController(ProductAdminService productAdminService) {
        this.productAdminService = productAdminService;
    }

    @GetMapping
    @Operation(summary = "List all products", description = "Fetch all products from the Product service with optional filtering")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions")
    })
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productAdminService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product details", description = "Fetch a specific product by ID from the Product service")
    @Parameter(name = "id", description = "Product ID (unique identifier)", required = true, example = "789")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ProductDTO> getProductById(
            @Parameter(description = "Product ID") @PathVariable Long id) {
        ProductDTO product = productAdminService.getProduct(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    @Operation(summary = "Create a new product", description = "Create a new product in the Product service")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid product data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only ROLE_ADMIN can create products")
    })
    public ResponseEntity<ProductDTO> createProduct(
            @Parameter(description = "Product data") @RequestBody ProductDTO dto,
            @Parameter(description = "Admin ID performing the action", required = true) @RequestParam Long adminId) {
        ProductDTO created = productAdminService.createProduct(dto, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product", description = "Update an existing product in the Product service")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - Invalid product data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductDTO> updateProduct(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Parameter(description = "Updated product data") @RequestBody ProductDTO dto,
            @Parameter(description = "Admin ID performing the action", required = true) @RequestParam Long adminId) {
        ProductDTO updated = productAdminService.updateProduct(id, dto, adminId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product", description = "Delete a product from the Product service")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only ROLE_ADMIN can delete products"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Product ID") @PathVariable Long id,
            @Parameter(description = "Admin ID performing the action", required = true) @RequestParam Long adminId) {
        productAdminService.deleteProduct(id, adminId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by name", description = "Search for products using a name query (partial match)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - Missing query parameter"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    public ResponseEntity<List<ProductDTO>> searchByName(
            @Parameter(description = "Product name (partial match)", required = true, example = "laptop") @RequestParam String name) {
        List<ProductDTO> results = productAdminService.searchByName(name);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Filter products by category", description = "Get all products in a specific category")
    public ResponseEntity<List<ProductDTO>> findByCategory(
            @Parameter(description = "Category name") @PathVariable String category) {
        List<ProductDTO> results = productAdminService.findByCategory(category);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/suggestions")
    @Operation(summary = "Get product suggestions", description = "Get top product suggestions for a query")
    public ResponseEntity<List<ProductDTO>> getSuggestions(
            @Parameter(description = "Search query") @RequestParam String query) {
        List<ProductDTO> suggestions = productAdminService.getSuggestions(query);
        return ResponseEntity.ok(suggestions);
    }
}
