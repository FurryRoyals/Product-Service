package theworldofpuppies.ProductService.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ApiResponse {
    private String message;
    private Boolean success;
    private Object data;
}
