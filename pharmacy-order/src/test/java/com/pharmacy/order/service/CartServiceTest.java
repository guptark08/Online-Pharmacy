package com.pharmacy.order.service;

import com.pharmacy.order.dto.CartDTO;
import com.pharmacy.order.dto.CartItemRequest;
import com.pharmacy.order.entity.Cart;
import com.pharmacy.order.entity.CartItem;
import com.pharmacy.order.repository.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void getCartCreatesNewCartWhenMissing() {
        when(cartRepository.findByUserEmail("user@example.com")).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart cart = invocation.getArgument(0);
            cart.setId(1L);
            return cart;
        });

        CartDTO cart = cartService.getCart("user@example.com");

        assertEquals(1L, cart.getId());
        assertEquals(0, cart.getItems().size());
    }

    @Test
    void addItemIncrementsQuantityForExistingMedicine() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserEmail("user@example.com");
        cart.setItems(new ArrayList<>());

        CartItem item = new CartItem();
        item.setId(10L);
        item.setMedicineId(5L);
        item.setMedicineName("Paracetamol");
        item.setPrice(BigDecimal.TEN);
        item.setQuantity(1);
        cart.getItems().add(item);

        CartItemRequest request = new CartItemRequest();
        request.setMedicineId(5L);
        request.setMedicineName("Paracetamol");
        request.setPrice(BigDecimal.TEN);
        request.setQuantity(2);

        when(cartRepository.findByUserEmail("user@example.com")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartDTO updated = cartService.addItem("user@example.com", request);

        assertEquals(1, updated.getItems().size());
        assertEquals(3, updated.getItems().get(0).getQuantity());
        assertEquals(BigDecimal.valueOf(30), updated.getTotalAmount());
    }

    @Test
    void updateItemRemovesLineWhenQuantityNonPositive() {
        Cart cart = new Cart();
        cart.setUserEmail("user@example.com");
        cart.setItems(new ArrayList<>());

        CartItem item = new CartItem();
        item.setId(10L);
        item.setMedicineId(5L);
        item.setPrice(BigDecimal.TEN);
        item.setQuantity(1);
        cart.getItems().add(item);

        when(cartRepository.findByUserEmail("user@example.com")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CartDTO updated = cartService.updateItem("user@example.com", 10L, 0);

        assertEquals(0, updated.getItems().size());
    }

    @Test
    void clearCartThrowsWhenCartMissing() {
        when(cartRepository.findByUserEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
            () -> cartService.clearCart("missing@example.com"));
    }

    @Test
    void clearCartEmptiesItems() {
        Cart cart = new Cart();
        cart.setUserEmail("user@example.com");
        cart.setItems(new ArrayList<>());
        cart.getItems().add(new CartItem());

        when(cartRepository.findByUserEmail("user@example.com")).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        cartService.clearCart("user@example.com");

        assertEquals(0, cart.getItems().size());
        verify(cartRepository).save(cart);
    }
}
