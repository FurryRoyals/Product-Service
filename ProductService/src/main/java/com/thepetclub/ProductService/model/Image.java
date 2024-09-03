package com.thepetclub.ProductService.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "images")
public class Image {
    @Id
    private String id;
    private String downloadUrl;
    private String fetchUrl;
    private String fileName;
    private String fileType;

    private String  productId;
}
