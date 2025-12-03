package iuh.fit.se.services;

import iuh.fit.se.dtos.CartDTO;
import iuh.fit.se.entities.Cart;

public interface CartService {

    CartDTO getCartByUsername(String username);

    CartDTO addToCart(String username, int productId, int quantity);

    CartDTO updateCartItem(String username, int productId, int quantity) ;

    CartDTO removefromCart(String username, int productId) ;

    void clearCart(String username) ;

}
