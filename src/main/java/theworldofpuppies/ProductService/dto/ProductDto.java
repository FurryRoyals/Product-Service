package theworldofpuppies.ProductService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import theworldofpuppies.ProductService.model.Image;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    private String id;
    private String name;
    private Double price;
    private int discount;
    private Double discountedPrice;
    private int inventory;
    private String description;
    private String categoryName;
    private List<Image> images = new ArrayList<>();
    private Image firstImage;
    private Boolean isFeatured;
    private Boolean isRecommended = false;
    private Boolean isRated;
    private Double averageStars;
    private Integer totalReviews;
}
