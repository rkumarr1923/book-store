package com.bookstore.service;

import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.domain.Address;
import com.bookstore.domain.User;
import com.bookstore.dto.request.AddressRequest;
import com.bookstore.dto.response.AddressResponse;
import com.bookstore.mapper.AddressMapper;
import com.bookstore.repository.AddressRepository;
import com.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for user address CRUD and default address management.
 */
@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository    userRepository;
    private final AddressMapper     addressMapper;

    // ── List ───────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AddressResponse> listAddresses(Long userId) {
        return addressRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(addressMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ── Create ─────────────────────────────────────────────────────────────────

    @Transactional
    public AddressResponse addAddress(Long userId, AddressRequest request) {
        User user = userRepository.getReferenceById(userId);

        // If this is marked as default, clear existing defaults first
        if (request.isDefault()) {
            addressRepository.clearDefaultByUserId(userId);
        }

        Address address = addressMapper.toEntity(request);
        address.setUser(user);
        address = addressRepository.save(address);
        return addressMapper.toResponse(address);
    }

    // ── Update ─────────────────────────────────────────────────────────────────

    @Transactional
    public AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        if (request.isDefault()) {
            addressRepository.clearDefaultByUserId(userId);
        }

        addressMapper.updateEntity(request, address);
        address = addressRepository.save(address);
        return addressMapper.toResponse(address);
    }

    // ── Delete ─────────────────────────────────────────────────────────────────

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        addressRepository.delete(address);
    }

    // ── Set Default ────────────────────────────────────────────────────────────

    @Transactional
    public AddressResponse setDefault(Long userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        addressRepository.clearDefaultByUserId(userId);
        address.setDefault(true);
        address = addressRepository.save(address);
        return addressMapper.toResponse(address);
    }
}
