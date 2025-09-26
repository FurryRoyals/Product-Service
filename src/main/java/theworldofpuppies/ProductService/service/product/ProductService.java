package theworldofpuppies.ProductService.service.product;

import com.mongodb.client.result.UpdateResult;
import theworldofpuppies.ProductService.dto.ProductDto;
import theworldofpuppies.ProductService.model.Product;
import theworldofpuppies.ProductService.request.AddProductRequest;
import theworldofpuppies.ProductService.request.ProductUpdateRequest;

import java.util.List;

public interface ProductService {
    ProductDto addProduct(AddProductRequest request);
    void addProductByCategory(List<AddProductRequest> requests, String category);
    ProductDto getProductById(String id);
    void deleteProductById(String id);
    ProductDto updateProductById(ProductUpdateRequest request, String id);
    List<ProductDto> getAllProducts(String cursor, int size);
    List<ProductDto> getFilteredProducts(String cursor, int size, String categoryName, String name);
    UpdateResult setProductField();
    List<ProductDto> getAllFeaturedProducts();
    List<ProductDto> getProductsByIds(List<String> productIds);
}
