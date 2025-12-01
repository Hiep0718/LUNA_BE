package iuh.fit.se.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Lấy đường dẫn gốc của dự án (Nơi chứa file pom.xml)
        String projectDir = System.getProperty("user.dir");

        // Trỏ vào thư mục "uploads" nằm trong dự án
        Path uploadPath = Paths.get(projectDir, "uploads");

        // Chuyển đường dẫn file hệ thống sang dạng URL resource
        // .toUri().toString() sẽ tự động thêm "file:/" vào đầu chuỗi chuẩn xác
        String resourcePath = uploadPath.toUri().toString();

        // Cấu hình: /uploads/** -> trỏ về thư mục vật lý
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourcePath);

        System.out.println("Serving images from: " + resourcePath);
    }
}