package com.thepetclub.ProductService.controller;

import com.thepetclub.ProductService.clients.auth.AuthClientRequest;
import com.thepetclub.ProductService.clients.auth.AuthClientResponse;
import com.thepetclub.ProductService.exception.AlreadyExistsException;
import com.thepetclub.ProductService.exception.ResourceNotFoundException;
import com.thepetclub.ProductService.exception.UnauthorizedException;
import com.thepetclub.ProductService.model.Category;
import com.thepetclub.ProductService.response.ApiResponse;
import com.thepetclub.ProductService.service.category.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("${api.prefix}/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final AuthClientRequest authClientRequest;

    @GetMapping("all")
    public ResponseEntity<ApiResponse> getAllCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(new ApiResponse("Found!", categories));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Error", INTERNAL_SERVER_ERROR));
        }
    }

    @PostMapping("add")
    public ResponseEntity<ApiResponse> addCategory(
            @RequestBody Category name,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
            AuthClientResponse authClientResponse = authClientRequest.validateAdmin(token);

            if (authClientResponse.isVerified()) {
                Category theCategory = categoryService.addCategory(name);
                return ResponseEntity.ok(new ApiResponse("Success", theCategory));
            } else {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(authClientResponse.getMessage(), null));
            }
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(CONFLICT).body(new ApiResponse(e.getMessage(), name));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCategoryById(@PathVariable String id) {
        try {
            Category theCategory = categoryService.getCategoryById(id);
            return ResponseEntity.ok(new ApiResponse("Found", theCategory));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getCategoryByName(@RequestParam String name) {
        try {
            Category theCategory = categoryService.getCategoryByName(name);
            return ResponseEntity.ok(new ApiResponse("Found", theCategory));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteCategoryById(
            @PathVariable String id,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
            AuthClientResponse authClientResponse = authClientRequest.validateAdmin(token);

            if (authClientResponse.isVerified()) {
                categoryService.deleteCategoryById(id);
                return ResponseEntity.ok(new ApiResponse("Category deleted successfully!", null));
            } else {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(authClientResponse.getMessage(), UNAUTHORIZED));
            }
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), UNAUTHORIZED));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), INTERNAL_SERVER_ERROR));
        }
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> updateCategory(
            @PathVariable String id,
            @RequestBody Category category,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
            AuthClientResponse authClientResponse = authClientRequest.validateAdmin(token);

            if (authClientResponse.isVerified()) {
                Category updatedCategory = categoryService.updateCategoryById(category, id);
                return ResponseEntity.ok(new ApiResponse("Update success!", updatedCategory));
            } else {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(authClientResponse.getMessage(), null));
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
