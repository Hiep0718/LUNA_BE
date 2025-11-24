package iuh.fit.se.services.impl;

import iuh.fit.se.services.ImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ImageServiceImpl implements ImageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp");

    @Override
    public String saveImage(MultipartFile file) {
        // Validate file
        List<String> errors = validateImageFile(file);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Invalid image file: " + String.join(", ", errors));
        }

        try {
            // Create directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String uniqueFileName = UUID.randomUUID() + "." + fileExtension;

            // Save file
            Path filePath = uploadPath.resolve(uniqueFileName);
            file.transferTo(filePath.toFile());

            // Return relative path to be stored in database
            return uploadDir + "/" + uniqueFileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImage(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            System.err.println("Failed to delete image: " + e.getMessage());
        }
    }

    @Override
    public List<String> validateImageFile(MultipartFile file) {
        List<String> errors = new ArrayList<>();

        if (file == null || file.isEmpty()) {
            errors.add("File is empty");
            return errors;
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            errors.add("File size exceeds maximum allowed size (5MB)");
        }

        // Check file extension
        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            errors.add("File type not allowed. Allowed types: " + ALLOWED_EXTENSIONS);
        }

        // Check MIME type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            errors.add("File is not a valid image");
        }

        return errors;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
