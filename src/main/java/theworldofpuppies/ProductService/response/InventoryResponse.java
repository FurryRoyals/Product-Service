package theworldofpuppies.ProductService.response;

import theworldofpuppies.ProductService.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class InventoryResponse {
    private String message;
    private boolean isAvailable;
    private Object data;
    private List<String> productIds;
    private Product product;
}
