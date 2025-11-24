package iuh.fit.se.services;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    String saveImage(MultipartFile file);
    void deleteImage(String filePath);
    List<String> validateImageFile(MultipartFile file);
}
