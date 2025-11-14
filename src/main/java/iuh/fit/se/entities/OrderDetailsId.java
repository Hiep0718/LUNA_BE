package iuh.fit.se.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailsId implements Serializable { // <-- Bắt buộc
    // Tên trường sẽ là "order" và "product"
    private int order;   // Khớp với kiểu PK 'int' của Orders
    private int product; // Khớp với kiểu PK 'int' của Products
}
