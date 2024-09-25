package com.thepetclub.ProductService.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Data
public class InventoryResponse {
    private String message;
    private boolean isAvailable;
    private Object data;
    private List<String> productIds;
}
