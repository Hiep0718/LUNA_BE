package iuh.fit.se.services;

import iuh.fit.se.entities.Orders;

public interface EmailService {
    /**
     * Gửi email xác nhận đơn hàng cho người dùng
     */
    void sendOrderConfirmationEmail(Orders order);
}
