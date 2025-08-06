package theworldofpuppies.ProductService.request;

import lombok.Data;


@Data
public class ProductUpdateRequest {
    private String  id;
    private String name;
    private Double price;
    private int discount;
    private Double discountedPrice;
    private int inventory;
    private String description;
    private String categoryName;
    private Boolean isRecommended;
    private Double rating;
}

