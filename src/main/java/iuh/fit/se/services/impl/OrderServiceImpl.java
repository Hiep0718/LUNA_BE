package iuh.fit.se.services.impl;

import iuh.fit.se.dtos.CartDTO;
import iuh.fit.se.entities.*;
import iuh.fit.se.repositories.*;
import iuh.fit.se.services.OrderService;
import iuh.fit.se.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CartRepository cartRepository;
    private final EmailService emailService; // Inject EmailService

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderDetailsRepository orderDetailsRepository,
                            ProductRepository productRepository,
                            UserRepository userRepository,
                            AddressRepository addressRepository,
                            CartRepository cartRepository,
                            EmailService emailService) { // Add EmailService parameter
        this.orderRepository = orderRepository;
        this.orderDetailsRepository = orderDetailsRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.cartRepository = cartRepository;
        this.emailService = emailService; // Initialize EmailService
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Orders checkout(String username, int addressId) {
        // 1. Validate Input & User
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // 2. Lấy Giỏ hàng từ Database
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty. Cannot checkout.");
        }

        // 3. Validate Address
        Addresses addr = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (addr.getUser() == null || !addr.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Invalid address. User does not own this address.");
        }

        // 4. Tính tổng tiền từ dữ liệu thực tế trong DB
        double totalAmount = cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        // 5. Tạo Order
        Orders order = new Orders();
        order.setUser(user);
        order.setAddress(addr);
        order.setOrderDate(Instant.now());
        order.setStatus("PENDING");
        order.setTotal(totalAmount);

        Orders savedOrder = orderRepository.save(order);

        // 6. Xử lý chi tiết Order & Trừ tồn kho
        List<OrderDetails> detailsList = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            Products product = cartItem.getProduct();

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            OrderDetails detail = new OrderDetails();
            detail.setOrder(savedOrder);
            detail.setProduct(product);
            detail.setQuantity(cartItem.getQuantity());
            detail.setPrice(product.getPrice());

            detailsList.add(detail);
        }

        orderDetailsRepository.saveAll(detailsList);

        // 7. Xóa giỏ hàng
        cart.getItems().clear();
        cartRepository.save(cart);

        try {
            emailService.sendOrderConfirmationEmail(savedOrder);
        } catch (Exception e) {
            // Log error nhưng không throw - đơn hàng vẫn được tạo thành công
            System.err.println("[EMAIL ERROR] Failed to send confirmation email: " + e.getMessage());
        }

        return savedOrder;
    }

    @Override
    public List<Orders> getOrderHistory(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return orderRepository.findByUser(user);
    }

    @Override
    public List<Orders> getOrdersByStatus(String status) {
        return orderRepository.findByStatusWithDetails(status);
    }

    @Override
    public List<Orders> getMyOrdersByStatus(String username, String status) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUserIdAndStatusWithDetails(user.getId(), status);
    }

    @Override
    @Transactional
    public Orders updateOrderStatus(int orderId, String newStatus) {
        Orders order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found ID: " + orderId));

        String oldStatus = order.getStatus();

        if (oldStatus.equals(newStatus)) {
            return order;
        }

        if (!"CANCELLED".equals(oldStatus) && "CANCELLED".equals(newStatus)) {
            List<OrderDetails> details = order.getOrderDetails();
            for (OrderDetails detail : details) {
                Products product = detail.getProduct();
                int quantityToReturn = detail.getQuantity();

                product.setStockQuantity(product.getStockQuantity() + quantityToReturn);
                productRepository.save(product);
            }
        }

        order.setStatus(newStatus);

        return orderRepository.save(order);
    }
}
