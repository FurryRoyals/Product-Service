package com.thepetclub.ProductService.clients.authClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "USER-SERVICE", url = "${user-service.url}")
public interface AuthClientRequest {

    @GetMapping("auth/validate-admin/{token}")
    AuthClientResponse validateAdmin(@PathVariable String token);
}
