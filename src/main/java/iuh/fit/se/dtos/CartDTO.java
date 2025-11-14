package iuh.fit.se.dtos;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CartDTO {
    private Map<Integer, CartItemDTO> items = new HashMap<>(); // Key là productId
    private double totalPrice = 0.0;

    // (Thêm getters/setters nếu không dùng record)

    // Logic thêm/sửa/xóa item
    public void addItem(CartItemDTO item) {
        items.put(item.productId(), item);
        updateTotalPrice();
    }

    public void updateItem(int productId, int quantity) {
        CartItemDTO item = items.get(productId);
        if (item != null) {
            if (quantity <= 0) {
                items.remove(productId);
            } else {
                // Tạo 1 record mới với quantity đã update
                items.put(productId, new CartItemDTO(
                        item.productId(), item.name(), item.price(), quantity, item.imageUrl()
                ));
            }
        }
        updateTotalPrice();
    }

    public void removeItem(int productId) {
        items.remove(productId);
        updateTotalPrice();
    }

    public void clearCart() {
        items.clear();
        totalPrice = 0.0;
    }

    public Collection<CartItemDTO> getItems() {
        return items.values();
    }

    private void updateTotalPrice() {
        totalPrice = items.values().stream()
                .mapToDouble(item -> item.price() * item.quantity())
                .sum();
    }

    public double getTotalPrice() {
        return totalPrice;
    }
}
