package com.thepetclub.ProductService.controller;

import com.amazonaws.util.CollectionUtils;
import com.mongodb.client.result.UpdateResult;
import com.thepetclub.ProductService.clients.AuthService;
import com.thepetclub.ProductService.exception.ResourceNotFoundException;
import com.thepetclub.ProductService.exception.UnauthorizedException;
import com.thepetclub.ProductService.model.Product;
import com.thepetclub.ProductService.request.AddProductRequest;
import com.thepetclub.ProductService.request.ProductUpdateRequest;
import com.thepetclub.ProductService.response.ApiResponse;
import com.thepetclub.ProductService.clients.AuthResponse;
import com.thepetclub.ProductService.service.product.ProductService;
import feign.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("${prefix}")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final AuthService authService;

    @PutMapping("set")
    public ResponseEntity<ApiResponse> setNewField() {
        try {
            UpdateResult products = productService.setProductField();
            if (products.getModifiedCount() == 0) {
                return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Failed to set new Field", false, null));
            }
            return ResponseEntity.ok(new ApiResponse("successfully set", true, products));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Failed to set new Field", false, null));
        }
    }

    @GetMapping("featured")
    public ResponseEntity<ApiResponse> getAllFeaturedProducts() {
        try {
            List<Product> featuredProducts = productService.getAllFeaturedProducts();
            if (featuredProducts.isEmpty()) {
                return ResponseEntity.status(NOT_FOUND).body(new ApiResponse("Empty", false, null));
            }
            return ResponseEntity.ok(new ApiResponse("Successful", true, featuredProducts));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), false, null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), false, null));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllProducts(
            @RequestParam(defaultValue = "") String cursor,
            @RequestParam(defaultValue = "10") int size,
            PagedResourcesAssembler<Product> pagedResourcesAssembler) {
        try {
            List<Product> products = productService.getAllProducts(cursor, size);
            String nextCursor = products.isEmpty() ? null : products.getLast().getId();
            Map<String, Object> response = new HashMap<>();
            response.put("products", products);
            response.put("nextCursor", nextCursor);
            return ResponseEntity.ok(new ApiResponse("Success", true, response));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), false, null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getFilteredProducts(
            @RequestParam(defaultValue = "") String cursor,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) String name) {
        try {
            List<Product> products = productService.getFilteredProducts(cursor, size, categoryName, name);
            String nextCursor = products.isEmpty() ? null : products.getLast().getId(); // Last ID as next cursor
            Map<String, Object> response = new HashMap<>();
            response.put("products", products);
            response.put("nextCursor", nextCursor);

            return ResponseEntity.ok(new ApiResponse("Success", true, response));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), false, null));
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable String productId) {
        try {
            Product product = productService.getProductById(productId);
            return ResponseEntity.ok(new ApiResponse("Success", true, product));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), false, null));
        }
    }

    @PostMapping("products/productIds")
    public ResponseEntity<ApiResponse> getProductsByIds(@RequestBody List<String> productIds) {
        try {
            List<Product> products = productService.getProductsByIds(productIds);
            if (!CollectionUtils.isNullOrEmpty(products)) {
                return ResponseEntity.ok(new ApiResponse("success", true, products));
            } else {
                return ResponseEntity.status(NOT_FOUND)
                        .body(new ApiResponse("No products found", false, null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse("Server error: " + e.getMessage(), false, null));
        }
    }


    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addProductByCategory(
            @RequestBody List<AddProductRequest> products,
            @RequestParam String category,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
            AuthResponse authResponse = authService.validateAdmin(token);
            if (authResponse.isVerified()) {
                if (category.isBlank()) {
                    return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse("category name missing", false, null));
                }
                productService.addProductByCategory(products, category);
                return ResponseEntity.ok(new ApiResponse("Add product success!", true, null));
            } else {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(authResponse.getMessage(), false, UNAUTHORIZED));
            }
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), false, INTERNAL_SERVER_ERROR));
        }
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<ApiResponse> updateProduct(
            @RequestBody ProductUpdateRequest request,
            @PathVariable String productId,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
            AuthResponse authResponse = authService.validateAdmin(token);

            if (authResponse.isVerified()) {
                Product product = productService.updateProductById(request, productId);
                return ResponseEntity.ok(new ApiResponse("Update product success!", true, product));
            } else {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(authResponse.getMessage(), false, UNAUTHORIZED));
            }
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), false, null));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), false, null));
        }
    }


    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<ApiResponse> deleteProduct(
            @PathVariable String productId,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
            AuthResponse authResponse = authService.validateAdmin(token);

            if (authResponse.isVerified()) {
                productService.deleteProductById(productId);
                return ResponseEntity.ok(new ApiResponse("Product deleted successfully!", true, null));
            } else {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(authResponse.getMessage(), false, UNAUTHORIZED));
            }
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), false, null));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), false, null));
        }
    }

}

