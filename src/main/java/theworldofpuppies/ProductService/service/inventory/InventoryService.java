package theworldofpuppies.ProductService.service.inventory;

import theworldofpuppies.ProductService.request.InventoryRequest;
import theworldofpuppies.ProductService.response.InventoryResponse;

import java.util.List;

public interface InventoryService {

    boolean isProductAvailable(String productId, int requiredQuantity);
    InventoryResponse isProductAvailable(List<InventoryRequest> availabilityRequests);
    void batchUpdateProductInventory(List<InventoryRequest> updateRequests);
    void restockProduct(String productId, int quantity);
}
