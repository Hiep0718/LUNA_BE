package iuh.fit.se.services.impl;

import iuh.fit.se.services.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ImageServiceImpl implements ImageService {

    // Tên thư mục chứa ảnh (nằm ngay tại root project)
    private static final String UPLOAD_DIR_NAME = "uploads";

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp");

    @Override
    public String saveImage(MultipartFile file) {
        // 1. Validate file
        List<String> errors = validateImageFile(file);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Invalid image file: " + String.join(", ", errors));
        }

        try {
            // 2. Xác định đường dẫn lưu file
            // Lấy đường dẫn gốc dự án (ví dụ: D:\Code\LUNA_BE)
            String projectDir = System.getProperty("user.dir");
            Path uploadPath = Paths.get(projectDir, UPLOAD_DIR_NAME);

            // 3. Tạo thư mục nếu chưa tồn tại
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 4. Tạo tên file unique
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String uniqueFileName = UUID.randomUUID() + "." + fileExtension;

            // 5. Lưu file vào ổ cứng
            Path filePath = uploadPath.resolve(uniqueFileName);
            file.transferTo(filePath.toFile());

            // 6. Trả về đường dẫn Web (URL) để lưu vào Database
            // Kết quả sẽ là: /uploads/ten-file.jpg
            return "/" + UPLOAD_DIR_NAME + "/" + uniqueFileName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to save image: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImage(String imageUrl) {
        try {
            // imageUrl: /uploads/abc.jpg -> Lấy tên file: abc.jpg
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

            // Tìm đường dẫn file vật lý
            String projectDir = System.getProperty("user.dir");
            Path filePath = Paths.get(projectDir, UPLOAD_DIR_NAME, fileName);

            // Xóa file
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            System.err.println("Failed to delete image: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error processing image path for deletion: " + e.getMessage());
        }
    }

    @Override
    public List<String> validateImageFile(MultipartFile file) {
        List<String> errors = new ArrayList<>();

        if (file == null || file.isEmpty()) {
            errors.add("File is empty");
            return errors;
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            errors.add("File size exceeds maximum allowed size (5MB)");
        }

        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            errors.add("File type not allowed. Allowed types: " + ALLOWED_EXTENSIONS);
        }

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