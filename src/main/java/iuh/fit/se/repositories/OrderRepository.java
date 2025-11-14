package iuh.fit.se.repositories;

import iuh.fit.se.entities.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Orders, Integer> {
    // Lấy lịch sử đơn hàng của 1 user
    List<Orders> findByUserId(Long userId);

    // Lấy danh sách đơn hàng sắp xếp theo ngày (cho Admin)
    List<Orders> findAllByOrderByOrderDateDesc();
}