package iuh.fit.se.repositories;

import iuh.fit.se.entities.Orders;
import iuh.fit.se.entities.User;
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

    @Query("SELECT DISTINCT o FROM Orders o " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.address " +
            "LEFT JOIN FETCH o.orderDetails od " +
            "LEFT JOIN FETCH od.product " +
            "WHERE o.user = ?1 " +
            "ORDER BY o.orderDate DESC")
    List<Orders> findByUser(User user);
    // 1. ADMIN: Lấy tất cả đơn theo status
    @Query("SELECT DISTINCT o FROM Orders o " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.address " +
            "LEFT JOIN FETCH o.orderDetails od " +
            "LEFT JOIN FETCH od.product " +
            "WHERE o.status = ?1 " +
            "ORDER BY o.orderDate DESC")
    List<Orders> findByStatusWithDetails(String status);

    // 2. USER: Lấy đơn của mình theo status
    @Query("SELECT DISTINCT o FROM Orders o " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.address " +
            "LEFT JOIN FETCH o.orderDetails od " +
            "LEFT JOIN FETCH od.product " +
            "WHERE o.user.id = ?1 AND o.status = ?2 " +
            "ORDER BY o.orderDate DESC")
    List<Orders> findByUserIdAndStatusWithDetails(Long userId, String status);
    // 1. Doanh thu 7 ngày qua
    @Query(value = "SELECT DATE_FORMAT(o.order_date, '%d/%m') as label, SUM(o.total) as value " +
            "FROM orders o " +
            "WHERE o.order_date >= DATE(NOW()) - INTERVAL 6 DAY " +
            "AND o.status = 'DELIVERED' " +
            "GROUP BY DATE(o.order_date), DATE_FORMAT(o.order_date, '%d/%m') " +
            "ORDER BY DATE(o.order_date)", nativeQuery = true)
    List<Object[]> getRevenueLast7Days();

    // 2. Doanh thu theo tháng
    @Query(value = "SELECT CONCAT('Tháng ', MONTH(o.order_date)) as label, SUM(o.total) as value " +
            "FROM orders o " +
            "WHERE YEAR(o.order_date) = YEAR(NOW()) " +
            "AND o.status = 'DELIVERED' " +
            "GROUP BY MONTH(o.order_date) " +
            "ORDER BY MONTH(o.order_date)", nativeQuery = true)
    List<Object[]> getRevenueByMonthCurrentYear();

    // 3. Đếm số đơn
    long countByStatus(String status);
}
