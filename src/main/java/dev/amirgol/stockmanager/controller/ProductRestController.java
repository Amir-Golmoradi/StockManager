package dev.amirgol.stockmanager.controller;


import dev.amirgol.stockmanager.dto.product.request.CreateProductRequest;
import dev.amirgol.stockmanager.dto.product.request.ProductDto;
import dev.amirgol.stockmanager.dto.product.response.StockResponse;
import dev.amirgol.stockmanager.service.product.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for product inventory management with security.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Stock Product Management", description = "APIs for managing product inventory in Stock Manager System")
@SecurityRequirement(name = "Bearer Authentication")
public class ProductRestController {

    private final ProductService productService;

    @Operation(summary = "Create a new product", description = "Creates a new product with initial stock of 100")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        ProductDto createdProduct = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @Operation(summary = "Get product details", description = "Retrieves full product information by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(
            @Parameter(description = "Product UUID") @PathVariable UUID id) {
        ProductDto product = productService.getProduct(id);
        return ResponseEntity.ok(product);
    }

    @Operation(summary = "Get product stock", description = "Retrieves current stock information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock information retrieved"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @GetMapping("/{id}/stock")
    public ResponseEntity<StockResponse> getProductStock(
            @Parameter(description = "Product UUID") @PathVariable UUID id) {
        StockResponse stockResponse = productService.getProductStock(id);
        return ResponseEntity.ok(stockResponse);
    }

    @Operation(summary = "Refill product stock", description = "Increases product stock by specified amount")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stock refilled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid refill amount"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping("/{id}/refill")
    public ResponseEntity<StockResponse> refillStock(
            @Parameter(description = "Product UUID") @PathVariable UUID id,
            @Parameter(description = "Amount to add") @RequestParam int amount) {
        StockResponse stockResponse = productService.refillStock(id, amount);
        return ResponseEntity.ok(stockResponse);
    }

    @Operation(summary = "Buy product", description = "Purchases product by reducing stock")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Purchase completed successfully"),
            @ApiResponse(responseCode = "400", description = "Insufficient stock or invalid quantity"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @PostMapping("/{id}/buy")
    public ResponseEntity<StockResponse> buyProduct(
            @Parameter(description = "Product UUID") @PathVariable UUID id,
            @Parameter(description = "Quantity to purchase") @RequestParam int quantity) {
        StockResponse stockResponse = productService.buyProduct(id, quantity);
        return ResponseEntity.ok(stockResponse);
    }

    @Operation(summary = "Reserve product", description = "Temporarily reserves product stock")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reservation completed successfully"),
            @ApiResponse(responseCode = "400", description = "Insufficient stock or invalid parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @PostMapping("/{id}/reserve")
    public ResponseEntity<StockResponse> reserveProduct(
            @Parameter(description = "Product UUID") @PathVariable UUID id,
            @Parameter(description = "Quantity to reserve") @RequestParam int quantity,
            @Parameter(description = "Reservation duration in minutes") @RequestParam int duration) {
        StockResponse stockResponse = productService.reserveProduct(id, quantity, duration);
        return ResponseEntity.ok(stockResponse);
    }
}