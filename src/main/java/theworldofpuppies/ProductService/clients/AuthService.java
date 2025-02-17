package theworldofpuppies.ProductService.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "USER-SERVICE", url = "${user-service.url}")
public interface AuthService {

    @GetMapping("auth/validate-admin")
    AuthResponse validateAdmin(@RequestParam String token);
}
