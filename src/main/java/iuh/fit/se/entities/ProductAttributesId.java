package iuh.fit.se.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributesId implements Serializable {
    private int product;
    private int attribute;
}
