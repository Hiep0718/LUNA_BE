package iuh.fit.se.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImageListDTO {
    private int id;
    private String imageUrl;
    private boolean isDefault;
    private String createdAt;
    private String updatedAt;
}
