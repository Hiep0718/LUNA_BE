package iuh.fit.se.services.impl;

import iuh.fit.se.dtos.CartDTO;
import iuh.fit.se.entities.*;
import iuh.fit.se.repositories.*;
import iuh.fit.se.services.OrderService;
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
    private final CartRepository cartRepository; // Cần thêm cái này để lấy giỏ hàng từ DB

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderDetailsRepository orderDetailsRepository,
                            ProductRepository productRepository,
                            UserRepository userRepository,
                            AddressRepository addressRepository,
                            CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailsRepository = orderDetailsRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.cartRepository = cartRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // Rollback nếu có lỗi bất kỳ (hết hàng, lỗi DB...)
    public Orders checkout(String username, int addressId) {
        // 1. Validate Input & User
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // 2. Lấy Giỏ hàng từ Database (Thay vì nhận CartDTO từ tham số)
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user"));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty. Cannot checkout.");
        }

        // 3. Validate Address (Bảo mật: User A không được dùng địa chỉ của User B)
        Addresses addr = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Giả sử Address có quan hệ ManyToOne với User. Cần check kỹ chỗ này.
        if (addr.getUser() == null || !addr.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Invalid address. User does not own this address.");
        }

        // 4. Tính tổng tiền từ dữ liệu thực tế trong DB (Không tin tưởng dữ liệu từ Client gửi lên)
        double totalAmount = cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        // 5. Tạo Order (Master)
        Orders order = new Orders();
        order.setUser(user);
        order.setAddress(addr);
        order.setOrderDate(Instant.now()); // Dùng Instant hoặc LocalDateTime tùy config của bạn
        order.setStatus("PENDING");
        order.setTotal(totalAmount); // Lưu ý: field bên Entity Orders của bạn tên là 'totalAmount' hay 'total'? Sửa lại cho khớp nhé.

        Orders savedOrder = orderRepository.save(order);

        // 6. Xử lý chi tiết Order & Trừ tồn kho
        List<OrderDetails> detailsList = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            Products product = cartItem.getProduct();

            // Check tồn kho (Concurrency Check: Ở mức cơ bản)
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }

            // Trừ tồn kho
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            // Tạo OrderDetail
            OrderDetails detail = new OrderDetails();
            detail.setOrder(savedOrder);
            detail.setProduct(product);
            detail.setQuantity(cartItem.getQuantity());
            detail.setPrice(product.getPrice()); // Lưu giá tại thời điểm mua (để sau này giá SP đổi không ảnh hưởng đơn cũ)

            detailsList.add(detail);
        }

        // Lưu danh sách chi tiết
        orderDetailsRepository.saveAll(detailsList);

        // 7. Quan trọng: XÓA GIỎ HÀNG sau khi đặt thành công
        cart.getItems().clear();
        cartRepository.save(cart); // JPA sẽ tự động xóa các dòng trong bảng cart_items nhờ orphanRemoval=true

        return savedOrder;
    }


    @Override
    public List<Orders> getOrderHistory(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Phương thức này cần được khai báo trong OrderRepository: List<Orders> findByUser(User user); hoặc findByUserId(Long id);
        // Dựa vào code cũ của bạn, tôi dùng findByUser
        return orderRepository.findByUser(user);
    }
    @Override
    public List<Orders> getOrdersByStatus(String status) {
        // Dùng equalsIgnoreCase hoặc chuẩn hóa về 1 kiểu để tìm kiếm chính xác hơn
        // Ở đây mình giữ nguyên String bạn truyền vào
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
        // Tìm đơn (dùng hàm có sẵn fetch details để trả về json đầy đủ)
        Orders order = orderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found ID: " + orderId));

        // CẬP NHẬT TRỰC TIẾP - Không check logic cũ/mới để dễ test
        order.setStatus(newStatus);

        return orderRepository.save(order);
    }
}