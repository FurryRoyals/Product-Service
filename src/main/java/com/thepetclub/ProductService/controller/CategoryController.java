package com.thepetclub.ProductService.controller;

import com.thepetclub.ProductService.clients.AuthService;
import com.thepetclub.ProductService.clients.AuthResponse;
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
@RequestMapping("{prefix}/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final AuthService authService;

    @GetMapping("all")
    public ResponseEntity<ApiResponse> getAllCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(new ApiResponse("Found!", true, categories));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Error", false, INTERNAL_SERVER_ERROR));
        }
    }

    @PostMapping("add")
    public ResponseEntity<ApiResponse> addCategory(
            @RequestBody Category name,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
            AuthResponse authResponse = authService.validateAdmin(token);

            if (authResponse.isVerified()) {
                Category theCategory = categoryService.addCategory(name);
                return ResponseEntity.ok(new ApiResponse("Success", true, theCategory));
            } else {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(authResponse.getMessage(), false, null));
            }
        } catch (AlreadyExistsException e) {
            return ResponseEntity.status(CONFLICT).body(new ApiResponse(e.getMessage(), false, name));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), false, null));
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCategoryById(@PathVariable String id) {
        try {
            Category theCategory = categoryService.getCategoryById(id);
            return ResponseEntity.ok(new ApiResponse("Found", true, theCategory));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), false, null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getCategoryByName(@RequestParam String name) {
        try {
            Category theCategory = categoryService.getCategoryByName(name);
            return ResponseEntity.ok(new ApiResponse("Found", true, theCategory));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), false, null));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteCategoryById(
            @PathVariable String id,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
            AuthResponse authResponse = authService.validateAdmin(token);

            if (authResponse.isVerified()) {
                categoryService.deleteCategoryById(id);
                return ResponseEntity.ok(new ApiResponse("Category deleted successfully!", true, null));
            } else {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(authResponse.getMessage(), false, UNAUTHORIZED));
            }
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), false, null));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(e.getMessage(), false, UNAUTHORIZED));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), false, INTERNAL_SERVER_ERROR));
        }
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> updateCategory(
            @PathVariable String id,
            @RequestBody Category category,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            String token = authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
            AuthResponse authResponse = authService.validateAdmin(token);

            if (authResponse.isVerified()) {
                Category updatedCategory = categoryService.updateCategoryById(category, id);
                return ResponseEntity.ok(new ApiResponse("Update success!", true, updatedCategory));
            } else {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(authResponse.getMessage(), false, null));
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
