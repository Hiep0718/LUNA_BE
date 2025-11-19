package iuh.fit.se.dtos;

import iuh.fit.se.entities.Products;

public record OrderDetailDTO(
        int productId,
        String productName,
        double price,
        int quantity,
        double subtotal
) {
    public OrderDetailDTO(int productId, String productName, double price, int quantity) {
        this(productId, productName, price, quantity, price * quantity);
    }
}
