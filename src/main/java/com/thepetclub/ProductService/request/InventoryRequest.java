package com.thepetclub.ProductService.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InventoryRequest {
    private String productId;
    private int quantity;
}
