package com.thepetclub.ProductService.service.inventory;

import com.thepetclub.ProductService.exception.ResourceNotFoundException;
import com.thepetclub.ProductService.model.Product;
import com.thepetclub.ProductService.repository.ProductRepository;
import com.thepetclub.ProductService.request.InventoryRequest;
import com.thepetclub.ProductService.response.InventoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;

    private final String productNotFound = "Product not found with: ";

    @Override
    public InventoryResponse isProductAvailable(List<InventoryRequest> availabilityRequests) {
        List<String> insufficientProducts = new ArrayList<>();
        for (InventoryRequest request : availabilityRequests) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(productNotFound + request.getProductId()));
            if (product.getInventory() < request.getQuantity()) {
                insufficientProducts.add(product.getId());
            }
        }
        if (!insufficientProducts.isEmpty()) {
            return new InventoryResponse("Not enough inventory for some products", false, null, insufficientProducts);
        }
        return new InventoryResponse("Products are available", true, null, null);
    }

    @Override
    public boolean isProductAvailable(String productId, int requiredQuantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(productNotFound + productId));
        return product.getInventory() >= requiredQuantity;
    }

    @Async
    public void batchUpdateProductInventory(List<InventoryRequest> updateRequests) {
        for (InventoryRequest updateRequest : updateRequests) {
            Product product = productRepository.findById(updateRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(productNotFound + updateRequest.getProductId()));

            int newInventory = product.getInventory() - updateRequest.getQuantity();
            if (newInventory < 0) {
                throw new IllegalArgumentException("Not enough inventory available for product: " + updateRequest.getProductId());
            }
            product.setInventory(newInventory);
            productRepository.save(product);
        }

    }

    @Async
    public void restockProduct(String productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(productNotFound + productId));

        product.setInventory(product.getInventory() + quantity);
        productRepository.save(product);
    }
}
