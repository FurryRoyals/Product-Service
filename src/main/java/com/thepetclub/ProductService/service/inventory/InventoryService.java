package com.thepetclub.ProductService.service.inventory;

import com.thepetclub.ProductService.request.InventoryRequest;
import com.thepetclub.ProductService.response.InventoryResponse;

import java.util.List;

public interface InventoryService {

    boolean isProductAvailable(String productId, int requiredQuantity);
    InventoryResponse isProductAvailable(List<InventoryRequest> availabilityRequests);
    void batchUpdateProductInventory(List<InventoryRequest> updateRequests);
    void restockProduct(String productId, int quantity);
}
