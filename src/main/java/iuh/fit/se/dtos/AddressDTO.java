package iuh.fit.se.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import iuh.fit.se.entities.Addresses;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AddressDTO(
        int id,
        String street,
        String city,
        String district,
        String province,
        boolean isDefault
) {
    public static AddressDTO fromEntity(Addresses address) {
        if (address == null) return null;
        return new AddressDTO(
                address.getId(),
                address.getStreet(),
                address.getCity(),
                address.getDistrict(),
                address.getProvince(),
                address.isDefault()
        );
    }
}
