package iuh.fit.se.repositories;

import iuh.fit.se.entities.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Orders, Integer> {
    // Lấy lịch sử đơn hàng của 1 user
    List<Orders> findByUserId(Long userId);

    @Query("SELECT DISTINCT o FROM Orders o " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.address " +
            "LEFT JOIN FETCH o.orderDetails od " +
            "LEFT JOIN FETCH od.product " +
            "ORDER BY o.orderDate DESC")
    List<Orders> findAllWithDetails();

    @Query("SELECT o FROM Orders o " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.address " +
            "LEFT JOIN FETCH o.orderDetails od " +
            "LEFT JOIN FETCH od.product " +
            "WHERE o.id = ?1")
    Optional<Orders> findByIdWithDetails(int id);

    @Query("SELECT DISTINCT o FROM Orders o " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.address " +
            "LEFT JOIN FETCH o.orderDetails od " +
            "LEFT JOIN FETCH od.product " +
            "WHERE o.user.id = ?1 " +
            "ORDER BY o.orderDate DESC")
    List<Orders> findByUserIdWithDetails(Long userId);
}
