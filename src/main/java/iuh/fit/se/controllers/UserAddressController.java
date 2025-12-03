package iuh.fit.se.controllers;

import iuh.fit.se.dtos.ApiResponse;
import iuh.fit.se.dtos.AddressDTO;
import iuh.fit.se.dtos.AddressRequestDTO;
import iuh.fit.se.entities.Addresses;
import iuh.fit.se.entities.User;
import iuh.fit.se.repositories.AddressRepository;
import iuh.fit.se.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for managing user addresses.
 * Provides endpoints to create, read, update, and delete addresses for authenticated users.
 * All endpoints return AddressDTO to avoid lazy loading issues with entity relationships.
 */
@RestController
@RequestMapping("/api/users/{userId}/addresses")
public class UserAddressController {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Autowired
    public UserAddressController(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get all addresses for a user.
     *
     * @param userId the user ID
     * @param auth authentication object
     * @return list of AddressDTO objects
     */
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getUserAddresses(
            @PathVariable Long userId,
            Authentication auth) {

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Unauthorized", "User must be logged in"));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<AddressDTO> addresses = user.getAddresses().stream()
                .map(AddressDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(200, "Addresses retrieved successfully", addresses));
    }

    /**
     * Get a specific address by ID.
     *
     * @param userId the user ID
     * @param addressId the address ID
     * @param auth authentication object
     * @return AddressDTO object
     */
    @GetMapping("/{addressId}")
    public ResponseEntity<ApiResponse<?>> getAddressById(
            @PathVariable Long userId,
            @PathVariable int addressId,
            Authentication auth) {

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Unauthorized", "User must be logged in"));
        }

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Addresses address = user.getAddresses().stream()
                    .filter(a -> a.getId() == addressId)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Address not found for this user"));

            return ResponseEntity.ok(ApiResponse.success(200, "Address retrieved successfully", AddressDTO.fromEntity(address)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Not Found", e.getMessage()));
        }
    }

    /**
     * Create a new address for a user.
     *
     * @param userId the user ID
     * @param addressRequest the address data
     * @param auth authentication object
     * @return created AddressDTO object
     */
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createAddress(
            @PathVariable Long userId,
            @RequestBody AddressRequestDTO addressRequest,
            Authentication auth) {

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Unauthorized", "User must be logged in"));
        }

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Addresses address = new Addresses();
            address.setUser(user);
            address.setStreet(addressRequest.street());
            address.setCity(addressRequest.city());
            address.setDistrict(addressRequest.district());
            address.setProvince(addressRequest.province());
            address.setDefault(addressRequest.isDefault());

            if (addressRequest.isDefault()) {
                user.getAddresses().forEach(a -> a.setDefault(false));
            }

            Addresses savedAddress = addressRepository.save(address);
            userRepository.save(user);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(201, "Address created successfully", AddressDTO.fromEntity(savedAddress)));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Bad Request", e.getMessage()));
        }
    }

    /**
     * Update an existing address.
     *
     * @param userId the user ID
     * @param addressId the address ID to update
     * @param addressRequest the updated address data
     * @param auth authentication object
     * @return updated AddressDTO object
     */
    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<?>> updateAddress(
            @PathVariable Long userId,
            @PathVariable int addressId,
            @RequestBody AddressRequestDTO addressRequest,
            Authentication auth) {

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Unauthorized", "User must be logged in"));
        }

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Addresses address = user.getAddresses().stream()
                    .filter(a -> a.getId() == addressId)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Address not found for this user"));

            address.setStreet(addressRequest.street());
            address.setCity(addressRequest.city());
            address.setDistrict(addressRequest.district());
            address.setProvince(addressRequest.province());

            if (addressRequest.isDefault()) {
                user.getAddresses().forEach(a -> {
                    if (a.getId() != addressId) {
                        a.setDefault(false);
                    }
                });
                address.setDefault(true);
            } else {
                address.setDefault(false);
            }

            Addresses updatedAddress = addressRepository.save(address);
            userRepository.save(user);

            return ResponseEntity.ok(ApiResponse.success(200, "Address updated successfully", AddressDTO.fromEntity(updatedAddress)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Not Found", e.getMessage()));
        }
    }

    /**
     * Delete an address.
     *
     * @param userId the user ID
     * @param addressId the address ID to delete
     * @param auth authentication object
     * @return success message
     */
    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<?>> deleteAddress(
            @PathVariable Long userId,
            @PathVariable int addressId,
            Authentication auth) {

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Unauthorized", "User must be logged in"));
        }

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Addresses address = user.getAddresses().stream()
                    .filter(a -> a.getId() == addressId)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Address not found for this user"));

            addressRepository.delete(address);
            userRepository.save(user);

            return ResponseEntity.ok(ApiResponse.success(200, "Address deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Not Found", e.getMessage()));
        }
    }

    /**
     * Set an address as default for a user.
     * This will unset default on all other addresses.
     *
     * @param userId the user ID
     * @param addressId the address ID to set as default
     * @param auth authentication object
     * @return updated AddressDTO object
     */
    @PutMapping("/{addressId}/set-default")
    public ResponseEntity<ApiResponse<?>> setDefaultAddress(
            @PathVariable Long userId,
            @PathVariable int addressId,
            Authentication auth) {

        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(401, "Unauthorized", "User must be logged in"));
        }

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Addresses address = user.getAddresses().stream()
                    .filter(a -> a.getId() == addressId)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Address not found for this user"));

            user.getAddresses().forEach(a -> a.setDefault(false));
            address.setDefault(true);

            Addresses updatedAddress = addressRepository.save(address);
            userRepository.save(user);

            return ResponseEntity.ok(ApiResponse.success(200, "Address set as default successfully", AddressDTO.fromEntity(updatedAddress)));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, "Not Found", e.getMessage()));
        }
    }
}
