package theworldofpuppies.ProductService.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Document(collection = "products")
public class Product {
    @Id
    private String id;
    private String name;
    private Double price;
    private int discount;
    private Double discountedPrice;
    private int inventory;
    private String description;
    private String categoryName;
    private Image firstImage;
    private Boolean isFeatured;
    private List<Image> images = new ArrayList<>();
    private Boolean isRecommended = false;

    public Product(String name,
                   Double price,
                   int discount,
                   Double discountedPrice,
                   int inventory,
                   String description,
                   String categoryName,
                   Boolean isFeatured,
                   Boolean isRecommended) {
        this.name = name;
        this.price = price;
        this.discount = discount;
        this.discountedPrice = discountedPrice;
        this.inventory = inventory;
        this.description = description;
        this.categoryName = categoryName;
        this.isFeatured = isFeatured;
        this.isRecommended = isRecommended;
    }
}
