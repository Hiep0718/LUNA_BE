package iuh.fit.se.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import iuh.fit.se.dtos.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .status(HttpServletResponse.SC_FORBIDDEN)
                .message("Access Denied")
                .errors(List.of("You do not have permission to access this resource"))
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
