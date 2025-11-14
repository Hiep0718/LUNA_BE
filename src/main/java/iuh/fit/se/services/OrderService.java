package iuh.fit.se.services;

import iuh.fit.se.dtos.CartDTO;
import iuh.fit.se.entities.Orders;

import java.util.List;

public interface OrderService {
    // Trả về Order đã tạo, hoặc throw exception
    Orders checkout(String username, CartDTO cart, int addressId); // Giả sử user chọn 1 addressId

    List<Orders> getOrderHistory(String username);
}
