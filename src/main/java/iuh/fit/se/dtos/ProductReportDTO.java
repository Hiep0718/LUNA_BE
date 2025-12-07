package iuh.fit.se.dtos;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductReportDTO {
    private int id;
    private String name;
    private String image;
    private double price;
    private long totalSold;
    private double totalRevenue;
}