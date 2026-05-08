package com.pharmacy.order.controller;

import com.pharmacy.order.dto.AddressDTO;
import com.pharmacy.order.security.JwtUtil;
import com.pharmacy.order.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<AddressDTO> saveAddress(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody AddressDTO request) {
        String email = extractEmail(authHeader);
        return ResponseEntity.ok(addressService.saveAddress(email, request));
    }

    @GetMapping
    public ResponseEntity<List<AddressDTO>> getMyAddresses(
            @RequestHeader("Authorization") String authHeader) {
        String email = extractEmail(authHeader);
        return ResponseEntity.ok(addressService.getMyAddresses(email));
    }

    private String extractEmail(String authHeader) {
        return jwtUtil.extractUsername(authHeader.substring(7));
    }
}