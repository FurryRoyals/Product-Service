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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;

    private final String productNotFound = "Product not found with: ";

    @Override
    public InventoryResponse isProductAvailable(List<InventoryRequest> availabilityRequests) {
        Map<String, Integer> totalRequested = new HashMap<>();
        for (InventoryRequest request : availabilityRequests) {
            if (request.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be > 0 for product: " + request.getProductId());
            }
            totalRequested.merge(request.getProductId(), request.getQuantity(), Integer::sum);
        }

        List<String> productIds = new ArrayList<>(totalRequested.keySet());
        Map<String, Product> productMap = productRepository.findAllById(productIds)
                .stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        List<String> insufficientProducts = new ArrayList<>();
        Map<String, Double> productPrices = new HashMap<>();

        for (String productId : productIds) {
            Product product = productMap.get(productId);
            if (product == null) {
                throw new ResourceNotFoundException(productNotFound + productId);
            }
            if (product.getDiscountedPrice() == null) {
                throw new IllegalStateException("Incomplete data for product: " + productId);
            }
            if (product.getInventory() < totalRequested.get(productId)) {
                insufficientProducts.add(productId);
            } else {
                productPrices.put(productId, product.getDiscountedPrice());
            }
        }

        if (!insufficientProducts.isEmpty()) {
            return new InventoryResponse("Not enough inventory for some products", false, null, insufficientProducts);
        }

        return new InventoryResponse("Products are available", true, productPrices, null);
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
