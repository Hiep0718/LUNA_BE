package iuh.fit.se.controllers;

import iuh.fit.se.dtos.ApiResponse;
import iuh.fit.se.dtos.CartDTO;
import iuh.fit.se.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<?>> viewCart(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).body(ApiResponse.error(401, "Unauthorized", "Please login"));

        // auth.getName() sẽ trả về username lấy từ JWT Token
        CartDTO cart = cartService.getCartByUsername(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(200, "Cart retrieved", cart));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<?>> addToCart(
            @RequestParam int productId,
            @RequestParam int quantity,
            Authentication auth) {

        if (auth == null) return ResponseEntity.status(401).body(ApiResponse.error(401, "Unauthorized", "Please login"));

        try {
            CartDTO cart = cartService.addToCart(auth.getName(), productId, quantity);
            return ResponseEntity.ok(ApiResponse.success(200, "Added to cart", cart));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Error", e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<?>> updateCart(
            @RequestParam int productId,
            @RequestParam int quantity,
            Authentication auth) {

        if (auth == null) return ResponseEntity.status(401).body(ApiResponse.error(401, "Unauthorized", "Please login"));

        CartDTO cart = cartService.updateCartItem(auth.getName(), productId, quantity);
        return ResponseEntity.ok(ApiResponse.success(200, "Cart updated", cart));
    }

    @DeleteMapping("/remove")
    public ResponseEntity<ApiResponse<?>> removeFromCart(
            @RequestParam int productId,
            Authentication auth) {

        if (auth == null) return ResponseEntity.status(401).body(ApiResponse.error(401, "Unauthorized", "Please login"));

        CartDTO cart = cartService.removefromCart(auth.getName(), productId);
        return ResponseEntity.ok(ApiResponse.success(200, "Item removed", cart));
    }
}