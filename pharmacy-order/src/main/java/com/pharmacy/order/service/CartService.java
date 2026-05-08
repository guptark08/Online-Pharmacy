package com.pharmacy.order.service;

import com.pharmacy.order.dto.*;
import com.pharmacy.order.entity.Cart;
import com.pharmacy.order.entity.CartItem;
import com.pharmacy.order.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    public CartDTO getCart(String email) {
        Cart cart = cartRepository.findByUserEmail(email)
            .orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setUserEmail(email);
                return cartRepository.save(newCart);
            });
        return mapToDTO(cart);
    }

    public CartDTO addItem(String email, CartItemRequest request) {
        Cart cart = cartRepository.findByUserEmail(email)
            .orElseGet(() -> {
                Cart newCart = new Cart();
                newCart.setUserEmail(email);
                return cartRepository.save(newCart);
            });

        CartItem existingItem = cart.getItems().stream()
            .filter(i -> i.getMedicineId().equals(request.getMedicineId()))
            .findFirst()
            .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(
                existingItem.getQuantity() + request.getQuantity());
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setMedicineId(request.getMedicineId());
            item.setMedicineName(request.getMedicineName());
            item.setPrice(request.getPrice());
            item.setQuantity(request.getQuantity());
            item.setRequiresPrescription(request.isRequiresPrescription());
            cart.getItems().add(item);
        }

        return mapToDTO(cartRepository.save(cart));
    }

    public CartDTO updateItem(String email, Long itemId, int quantity) {
        Cart cart = cartRepository.findByUserEmail(email)
            .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cart.getItems().stream()
            .filter(i -> i.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Item not found"));

        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
        }

        return mapToDTO(cartRepository.save(cart));
    }

    public CartDTO removeItem(String email, Long itemId) {
        Cart cart = cartRepository.findByUserEmail(email)
            .orElseThrow(() -> new RuntimeException("Cart not found"));
        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        return mapToDTO(cartRepository.save(cart));
    }

    public void clearCart(String email) {
        Cart cart = cartRepository.findByUserEmail(email)
            .orElseThrow(() -> new RuntimeException("Cart not found"));
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    public CartDTO mapToDTO(Cart cart) {
        List<CartItemDTO> itemDTOs = cart.getItems().stream()
            .map(this::mapItemToDTO)
            .collect(Collectors.toList());

        BigDecimal total = itemDTOs.stream()
            .map(CartItemDTO::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartDTO dto = new CartDTO();
        dto.setId(cart.getId());
        dto.setItems(itemDTOs);
        dto.setTotalAmount(total);
        dto.setTotalItems(itemDTOs.size());
        return dto;
    }

    private CartItemDTO mapItemToDTO(CartItem item) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(item.getId());
        dto.setMedicineId(item.getMedicineId());
        dto.setMedicineName(item.getMedicineName());
        dto.setPrice(item.getPrice());
        dto.setQuantity(item.getQuantity());
        dto.setSubtotal(item.getPrice().multiply(
            BigDecimal.valueOf(item.getQuantity())));
        dto.setRequiresPrescription(item.isRequiresPrescription());
        return dto;
    }
}