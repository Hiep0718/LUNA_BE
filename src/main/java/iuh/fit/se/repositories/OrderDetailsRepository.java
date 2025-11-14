package iuh.fit.se.repositories;

import iuh.fit.se.entities.OrderDetails;
import iuh.fit.se.entities.OrderDetailsId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailsRepository extends JpaRepository<OrderDetails, OrderDetailsId> {
    // Đếm xem sản phẩm đã có trong đơn hàng nào chưa (để kiểm tra ràng buộc xóa)
    long countByProductId(int productId);
}
