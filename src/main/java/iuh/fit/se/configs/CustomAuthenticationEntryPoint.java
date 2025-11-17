package iuh.fit.se.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.se.dtos.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .status(HttpServletResponse.SC_UNAUTHORIZED)
                .message("Unauthorized")
                .errors(List.of("Missing or invalid authentication token"))
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
