package iuh.fit.se.services.impl;

import iuh.fit.se.dtos.CartDTO;
import iuh.fit.se.dtos.CartItemDTO;
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

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, OrderDetailsRepository orderDetailsRepository, ProductRepository productRepository, UserRepository userRepository, AddressRepository addressRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailsRepository = orderDetailsRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
    }


    @Override
    @Transactional // Very important for transaction rollback on error!
    public Orders checkout(String username, CartDTO cart, int addressId) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Addresses addr = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (!addr.getUser().getId().equals(user.getId())) {
            throw new SecurityException("User does not own this address");
        }

        // 1. Create Order
        Orders order = new Orders();
        order.setUser(user);
        order.setAddress(addr);
        order.setOrderDate(Instant.now());
        order.setStatus("PENDING");
        order.setTotal(cart.getTotalPrice());

        Orders savedOrder = orderRepository.save(order);

        // 2. Create OrderDetails (check stock + decrease inventory)
        List<OrderDetails> detailsList = new ArrayList<>();
        for (CartItemDTO item : cart.getItems()) {
            Products p = productRepository.findById(item.productId())
                    .orElseThrow(() -> new RuntimeException("Product " + item.name() + " not found"));

            if (p.getStockQuantity() < item.quantity()) {
                throw new RuntimeException("Not enough stock for " + p.getName());
            }

            p.setStockQuantity(p.getStockQuantity() - item.quantity());
            productRepository.save(p);

            OrderDetails detail = new OrderDetails();
            detail.setOrder(savedOrder);
            detail.setProduct(p);
            detail.setQuantity(item.quantity());
            detail.setPrice(item.price());

            detailsList.add(detail);
        }

        orderDetailsRepository.saveAll(detailsList);

        return savedOrder;
    }

    @Override
    public List<Orders> getOrderHistory(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUserId(user.getId());
    }
}
