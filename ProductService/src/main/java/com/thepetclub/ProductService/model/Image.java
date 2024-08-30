package com.thepetclub.ProductService.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "images")
public class Image {
    @Id
    private ObjectId id;
    private String downloadUrl;
    private String thumbnailDownloadUrl;
    private String fileName;
    private String fileType;
    private byte[] thumbnail;

    private ObjectId productId;
}
