package iuh.fit.se.controllers;

import iuh.fit.se.dtos.ApiResponse;
import iuh.fit.se.dtos.CartDTO;
import iuh.fit.se.dtos.CartItemDTO;
import iuh.fit.se.entities.Products;
import iuh.fit.se.repositories.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final ProductRepository productRepository; // Cần để lấy thông tin SP

    @Autowired
    public CartController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Lấy giỏ hàng từ Session
    private CartDTO getCart(HttpSession session) {
        CartDTO cart = (CartDTO) session.getAttribute("CART");
        if (cart == null) {
            cart = new CartDTO();
            session.setAttribute("CART", cart);
        }
        return cart;
    }

    // Yêu cầu: Xem giỏ hàng
    @GetMapping
    public ResponseEntity<ApiResponse<?>> viewCart(HttpSession session) {
        return ResponseEntity.ok(ApiResponse.success(200, "Cart retrieved successfully", getCart(session)));
    }

    // Yêu cầu: Thêm sản phẩm vào giỏ
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<?>> addToCart(@RequestParam int productId, @RequestParam int quantity, HttpSession session) {

        Optional<Products> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty() || !productOpt.get().isActive()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Not Found", "Product not found"));
        }

        if (quantity <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", "Quantity must be positive"));
        }

        Products p = productOpt.get();
        if (p.getStockQuantity() < quantity) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", "Not enough stock"));
        }

        CartDTO cart = getCart(session);

        // Lấy ảnh đầu tiên (nếu có)
        String imageUrl = p.getProductImages().isEmpty() ? null : p.getProductImages().get(0).getImageUrl();

        CartItemDTO item = new CartItemDTO(p.getId(), p.getName(), p.getPrice(), quantity, imageUrl);
        cart.addItem(item);

        return ResponseEntity.ok(ApiResponse.success(200, "Product added to cart successfully", cart));
    }

    // Yêu cầu: Sửa số lượng (hoặc xóa)
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<?>> updateCart(@RequestParam int productId, @RequestParam int quantity, HttpSession session) {
        CartDTO cart = getCart(session);
        cart.updateItem(productId, quantity); // Logic update (hoặc xóa nếu quantity=0) đã ở trong DTO
        return ResponseEntity.ok(ApiResponse.success(200, "Cart updated successfully", cart));
    }

    // Endpoint xóa hẳn
    @DeleteMapping("/remove")
    public ResponseEntity<ApiResponse<?>> removeFromCart(@RequestParam int productId, HttpSession session) {
        CartDTO cart = getCart(session);
        cart.removeItem(productId);
        return ResponseEntity.ok(ApiResponse.success(200, "Product removed from cart successfully", cart));
    }
}
