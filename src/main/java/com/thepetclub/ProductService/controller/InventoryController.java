package com.thepetclub.ProductService.controller;

import com.thepetclub.ProductService.clients.AuthResponse;
import com.thepetclub.ProductService.clients.AuthService;
import com.thepetclub.ProductService.exception.ResourceNotFoundException;
import com.thepetclub.ProductService.request.InventoryRequest;
import com.thepetclub.ProductService.response.ApiResponse;
import com.thepetclub.ProductService.response.InventoryResponse;
import com.thepetclub.ProductService.service.inventory.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("${prefix}/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;
    private final AuthService authService;

    @PostMapping("/isAvailable")
    public ResponseEntity<InventoryResponse> checkInventoryAvailability(
            @RequestBody(required = false) List<InventoryRequest> availabilityRequests,
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) Integer requiredQuantity) {
        try {
            boolean isAvailable;
            if (availabilityRequests != null && !availabilityRequests.isEmpty()) {
                InventoryResponse response = inventoryService.isProductAvailable(availabilityRequests);
                isAvailable = response.isAvailable();
                if (isAvailable) {
                    return ResponseEntity.ok(new InventoryResponse(response.getMessage(), true, response.getData(), null));
                } else {
                    return ResponseEntity.status(BAD_REQUEST)
                            .body(new InventoryResponse(response.getMessage(), false, null, response.getProductIds()));
                }
            } else if (productId != null && requiredQuantity != null) {
                isAvailable = inventoryService.isProductAvailable(productId, requiredQuantity);
                if (isAvailable) {
                    return ResponseEntity.ok(new InventoryResponse("Product is available", true, null, null));
                } else {
                    return ResponseEntity.status(BAD_REQUEST)
                            .body(new InventoryResponse("Product is not available in the required quantity", false, null, null));
                }
            } else {
                return ResponseEntity.status(BAD_REQUEST)
                        .body(new InventoryResponse("Invalid request parameters", false, null, null));
            }

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new InventoryResponse(e.getMessage(), false, null, null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new InventoryResponse("An error occurred while checking product availability", false, null, null));
        }
    }

    @PutMapping("/update-stock")
    public ResponseEntity<InventoryResponse> batchUpdateInventoryStock(
            @RequestBody List<InventoryRequest> updateRequests) {
        try {
            inventoryService.batchUpdateProductInventory(updateRequests);
            return ResponseEntity.ok(new InventoryResponse("inventory has been updated", true, null, null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new InventoryResponse(e.getMessage(), false, null, null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(BAD_REQUEST).body(new InventoryResponse(e.getMessage(), false, null, null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                    .body(new InventoryResponse("Error occurred while updating the inventory", false, null, null));
        }
    }

    @PutMapping("/restock")
    public ResponseEntity<ApiResponse> restockInventory(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam String productId,
            @RequestParam int quantity) {
        try {
            String token = extractToken(authorizationHeader);
            AuthResponse authResponse = authService.validateAdmin(token);
            if (authResponse.isVerified()) {
                inventoryService.restockProduct(productId, quantity);
                return ResponseEntity.ok(new ApiResponse("Inventory has been updated successfully", true, null));
            } else {
                return ResponseEntity.status(UNAUTHORIZED).body(new ApiResponse(authResponse.getMessage(), false, null));
            }
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), false, null));
        } catch (Exception e) {
            return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ApiResponse("Error occurred while restocking inventory", false, null));
        }
    }

    private String extractToken(String authorizationHeader) {
        return authorizationHeader.startsWith("Bearer ") ? authorizationHeader.substring(7) : authorizationHeader;
    }

}
