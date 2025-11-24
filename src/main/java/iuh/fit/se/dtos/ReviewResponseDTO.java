package iuh.fit.se.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDTO {
    private int id;
    private int productId;
    private String productName;
    private Long userId;
    private String userName;
    private int rating;
    private String comment;
    private Instant createdAt;
}
