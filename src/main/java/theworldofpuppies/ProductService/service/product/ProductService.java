package theworldofpuppies.ProductService.service.product;

import com.mongodb.client.result.UpdateResult;
import theworldofpuppies.ProductService.model.Product;
import theworldofpuppies.ProductService.request.AddProductRequest;
import theworldofpuppies.ProductService.request.ProductUpdateRequest;

import java.util.List;

public interface ProductService {
    Product addProduct(AddProductRequest request);
    void addProductByCategory(List<AddProductRequest> requests, String category);
    Product getProductById(String id);
    void deleteProductById(String id);
    Product updateProductById(ProductUpdateRequest request, String id);
    List<Product> getAllProducts(String cursor, int size);
    List<Product> getFilteredProducts(String cursor, int size, String categoryName, String name);
    UpdateResult setProductField();
    List<Product> getAllFeaturedProducts();
    List<Product> getProductsByIds(List<String> productIds);
}
