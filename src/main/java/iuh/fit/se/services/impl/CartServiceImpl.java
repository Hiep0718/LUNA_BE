package iuh.fit.se.services.impl;

import iuh.fit.se.dtos.CartDTO;
import iuh.fit.se.dtos.CartItemDTO;
import iuh.fit.se.entities.Cart;
import iuh.fit.se.entities.CartItem;
import iuh.fit.se.entities.Products;
import iuh.fit.se.entities.User;
import iuh.fit.se.repositories.CartRepository;
import iuh.fit.se.repositories.ProductRepository;
import iuh.fit.se.repositories.UserRepository;
import iuh.fit.se.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Override
    // Lấy giỏ hàng của user hiện tại (nếu chưa có thì tạo mới)
    public CartDTO getCartByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });

        return convertToDTO(cart);
    }
    @Override
    public CartDTO addToCart(String username, int productId, int quantity) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser(user).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });

        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Kiểm tra tồn kho
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Not enough stock");
        }

        // Kiểm tra xem sản phẩm đã có trong giỏ chưa
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId() == productId) // Giả sử Product ID là int/long tương thích
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.addItem(newItem); // Helper method trong Entity Cart
        }

        cartRepository.save(cart);
        return convertToDTO(cart);
    }
    @Override
    public CartDTO updateCartItem(String username, int productId, int quantity) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId() == productId)
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            if (quantity <= 0) {
                cart.removeItem(item);
            } else {
                item.setQuantity(quantity);
            }
            cartRepository.save(cart);
        }
        return convertToDTO(cart);
    }
    @Override
    public CartDTO removefromCart(String username, int productId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.getItems().removeIf(item -> item.getProduct().getId() == productId);
        cartRepository.save(cart);

        return convertToDTO(cart);
    }
    @Override
    public void clearCart(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Cart cart = cartRepository.findByUser(user).orElse(null);
        if(cart != null) {
            cart.getItems().clear();
            cartRepository.save(cart);
        }
    }

    // Helper: Chuyển Entity sang DTO để trả về Controller
    private CartDTO convertToDTO(Cart cart) {
        CartDTO cartDTO = new CartDTO();
        for (CartItem item : cart.getItems()) {
            Products p = item.getProduct();
            String imgUrl = (p.getProductImages() != null && !p.getProductImages().isEmpty())
                    ? p.getProductImages().get(0).getImageUrl()
                    : ""; // Xử lý null ảnh

            CartItemDTO itemDTO = new CartItemDTO(
                    p.getId(),
                    p.getName(),
                    p.getPrice(),
                    item.getQuantity(),
                    imgUrl
            );
            cartDTO.addItem(itemDTO);
        }
        return cartDTO;
    }
}