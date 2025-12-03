package iuh.fit.se.dtos;

/**
 * DTO for creating or updating an address.
 * Used to receive address data from client requests.
 */
public record AddressRequestDTO(
        String street,
        String city,
        String district,
        String province,
        boolean isDefault
) {}
