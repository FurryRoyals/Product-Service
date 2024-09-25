package com.thepetclub.ProductService.clients;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class AuthResponse {
    private String message;
    private boolean isVerified;
}
