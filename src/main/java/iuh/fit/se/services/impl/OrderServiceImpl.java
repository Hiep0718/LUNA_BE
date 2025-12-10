package iuh.fit.se.services.impl;

import iuh.fit.se.entities.*;
import iuh.fit.se.repositories.*;
import iuh.fit.se.services.OrderService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CartRepository cartRepository;

    // 1. Inject EntityManager để xử lý lỗi Session
    @PersistenceContext
    private EntityManager entityManager;

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
    @Transactional(rollbackFor = Exception.class)
    public Orders checkout(String username, int addressId) {
        // Validation cơ bản
        if (username == null || username.trim().isEmpty()) throw new IllegalArgumentException("Username empty");
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        Cart cart = cartRepository.findByUser(user).orElseThrow(() -> new RuntimeException("Cart not found"));
        if (cart.getItems() == null || cart.getItems().isEmpty()) throw new IllegalArgumentException("Cart empty");
        Addresses addr = addressRepository.findById(addressId).orElseThrow(() -> new RuntimeException("Address not found"));
        if (addr.getUser() == null || !addr.getUser().getId().equals(user.getId())) throw new SecurityException("Invalid address");

        // --- GỘP SẢN PHẨM TRÙNG (Map Logic) ---
        Map<Integer, CartItem> mergedItemsMap = new HashMap<>();
        for (CartItem item : cart.getItems()) {
            Integer productId = item.getProduct().getId();
            if (mergedItemsMap.containsKey(productId)) {
                CartItem existing = mergedItemsMap.get(productId);
                existing.setQuantity(existing.getQuantity() + item.getQuantity());
            } else {
                mergedItemsMap.put(productId, item);
            }
        }

        // Tính tổng
        double totalAmount = mergedItemsMap.values().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        // Tạo Order Header
        Orders order = new Orders();
        order.setUser(user);
        order.setAddress(addr);
        order.setShippingFee(totalAmount * 0.05);
        order.setTax(totalAmount * 0.05);
        order.setOrderDate(Instant.now());
        order.setStatus("PENDING");
        order.setTotal(totalAmount + totalAmount * 0.1);

        // Lưu Order để lấy ID
        Orders savedOrder = orderRepository.save(order);

        // Xử lý chi tiết
        List<OrderDetails> detailsList = new ArrayList<>();

        for (CartItem cartItem : mergedItemsMap.values()) {
            // Lấy product tươi từ DB để đảm bảo chính xác nhất
            Products product = productRepository.findById(cartItem.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Hết hàng: " + product.getName());
            }

            // Trừ tồn kho
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            // Tạo Detail
            OrderDetails detail = new OrderDetails();
            detail.setOrder(savedOrder);
            detail.setProduct(product);
            detail.setQuantity(cartItem.getQuantity());
            detail.setPrice(product.getPrice());

            detailsList.add(detail);
        }

        // Lưu danh sách chi tiết
        orderDetailsRepository.saveAll(detailsList);

        // Xóa giỏ hàng
        cart.getItems().clear();
        cartRepository.save(cart);

        // --- FIX LỖI "A different object..." & LỖI EMAIL KHÔNG HIỆN SẢN PHẨM ---

        // 1. Đẩy mọi thay đổi xuống Database ngay lập tức
        entityManager.flush();

        // 2. Xóa sạch bộ nhớ đệm (Session) của Hibernate
        // Điều này làm cho Hibernate "quên" đi các object đang xung đột ID
        entityManager.clear();

        // 3. Tải lại Order từ Database
        // Vì Session đã clear, Hibernate buộc phải SELECT lại từ DB.
        // Lúc này nó sẽ lấy được Order kèm theo danh sách OrderDetails đầy đủ và sạch sẽ.
        Orders finalOrder = orderRepository.findById(savedOrder.getId())
                .orElseThrow(() -> new RuntimeException("Error retrieving order"));

        return finalOrder;
    }

    // ... Các hàm khác giữ nguyên
    @Override
    public List<Orders> getOrderHistory(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUser(user);
    }
    @Override
    public List<Orders> getOrdersByStatus(String status) { return orderRepository.findByStatusWithDetails(status); }
    @Override
    public List<Orders> getMyOrdersByStatus(String username, String status) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUserIdAndStatusWithDetails(user.getId(), status);
    }
    @Override
    @Transactional
    public Orders updateOrderStatus(int orderId, String newStatus) {
        Orders order = orderRepository.findByIdWithDetails(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        String oldStatus = order.getStatus();
        if (oldStatus.equals(newStatus)) return order;
        if (!"CANCELLED".equals(oldStatus) && "CANCELLED".equals(newStatus)) {
            for (OrderDetails detail : order.getOrderDetails()) {
                Products p = detail.getProduct();
                p.setStockQuantity(p.getStockQuantity() + detail.getQuantity());
                productRepository.save(p);
            }
        }
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
}