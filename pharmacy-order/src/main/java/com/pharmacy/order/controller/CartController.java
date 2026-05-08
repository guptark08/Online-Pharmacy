package com.pharmacy.order.controller;

import com.pharmacy.order.dto.*;
import com.pharmacy.order.security.JwtUtil;
import com.pharmacy.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<CartDTO> getCart(
            @RequestHeader("Authorization") String authHeader) {
        String email = extractEmail(authHeader);
        return ResponseEntity.ok(cartService.getCart(email));
    }

    @PostMapping("/items")
    public ResponseEntity<CartDTO> addItem(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CartItemRequest request) {
        String email = extractEmail(authHeader);
        return ResponseEntity.ok(cartService.addItem(email, request));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> updateItem(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long itemId,
            @RequestParam int quantity) {
        String email = extractEmail(authHeader);
        return ResponseEntity.ok(cartService.updateItem(email, itemId, quantity));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDTO> removeItem(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long itemId) {
        String email = extractEmail(authHeader);
        return ResponseEntity.ok(cartService.removeItem(email, itemId));
    }

    @DeleteMapping
    public ResponseEntity<String> clearCart(
            @RequestHeader("Authorization") String authHeader) {
        String email = extractEmail(authHeader);
        cartService.clearCart(email);
        return ResponseEntity.ok("Cart cleared");
    }

    private String extractEmail(String authHeader) {
        return jwtUtil.extractUsername(authHeader.substring(7));
    }
}