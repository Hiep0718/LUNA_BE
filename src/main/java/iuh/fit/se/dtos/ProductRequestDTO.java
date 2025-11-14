package iuh.fit.se.dtos;

public record ProductRequestDTO(
        String name,
        String description,
        double price,
        int stockQuantity,
        boolean isActive,
        int categoryId,
        int brandId
        // Thêm list attributes, images nếu cần
) {}
