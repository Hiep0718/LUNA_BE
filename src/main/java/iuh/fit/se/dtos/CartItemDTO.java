package iuh.fit.se.dtos;

public record CartItemDTO(
        int productId,
        String name,
        double price,
        int quantity,
        String imageUrl // Lấy ảnh đầu tiên của sản phẩm
) {
}
