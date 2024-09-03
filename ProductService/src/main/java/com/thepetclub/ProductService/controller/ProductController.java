package com.thepetclub.ProductService.controller;

import com.thepetclub.ProductService.clients.authClient.AuthClientRequest;
import com.thepetclub.ProductService.exception.ResourceNotFoundException;
import com.thepetclub.ProductService.exception.UnauthorizedException;
import com.thepetclub.ProductService.model.Product;
import com.thepetclub.ProductService.request.AddProductRequest;
import com.thepetclub.ProductService.request.ProductUpdateRequest;
import com.thepetclub.ProductService.response.ApiResponse;
import com.thepetclub.ProductService.clients.authClient.AuthClientResponse;
import com.thepetclub.ProductService.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final AuthClientRequest authClientRequest;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(new ApiResponse("Success", products));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable String productId) {
        try {
            Product product = productService.getProductById(productId);
            return ResponseEntity.ok(new ApiResponse("Success", product));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String categoryName) {
        try {
            List<Product> products;
            if (name != null) {
                products = productService.getProductsByName(name);
            } else if (categoryName != null) {
                products = productService.getProductsByCategory(categoryName);
            } else {
                throw new IllegalArgumentException("At least one query parameter is required.");
            }
            return ResponseEntity.ok(new ApiResponse("Success", products));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(BAD_REQUEST).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addProduct(
            @RequestBody AddProductRequest request,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
            AuthClientResponse authClientResponse = authClientRequest.validateAdmin(token);
            if (authClientResponse.isVerified()) {
                Product product = productService.addProduct(request);
                return ResponseEntity.ok(new ApiResponse("Add product success!", product));
            } else {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(authClientResponse.getMessage(), UNAUTHORIZED));
            }
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), INTERNAL_SERVER_ERROR));
        }
    }


    @PutMapping("/update/{productId}")
    public ResponseEntity<ApiResponse> updateProduct(
            @RequestBody ProductUpdateRequest request,
            @PathVariable String productId,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
            AuthClientResponse authClientResponse = authClientRequest.validateAdmin(token);

            if (authClientResponse.isVerified()) {
                Product product = productService.updateProductById(request, productId);
                return ResponseEntity.ok(new ApiResponse("Update product success!", product));
            } else {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(authClientResponse.getMessage(), UNAUTHORIZED));
            }
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
        }
    }


    @DeleteMapping("/delete/{productId}")
    public ResponseEntity<ApiResponse> deleteProduct(
            @PathVariable String productId,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
            AuthClientResponse authClientResponse = authClientRequest.validateAdmin(token);

            if (authClientResponse.isVerified()) {
                productService.deleteProductById(productId);
                return ResponseEntity.ok(new ApiResponse("Product deleted successfully!", null));
            } else {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(authClientResponse.getMessage(), UNAUTHORIZED));
            }
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
        }
    }

}

