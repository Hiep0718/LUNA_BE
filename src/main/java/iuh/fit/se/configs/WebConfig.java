package iuh.fit.se.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Đảm bảo đường dẫn kết thúc bằng /
        String fileResourcePath = "file:" + uploadDir;
        if (!uploadDir.endsWith(File.separator)) {
            fileResourcePath += "/";
        }

        // Ánh xạ URL /uploads/** tới thư mục vật lý đã cấu hình
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(fileResourcePath);
    }
}
