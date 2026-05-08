package com.pharmacy.order.service;

import com.pharmacy.order.client.CatalogClient;
import com.pharmacy.order.dto.*;
import com.pharmacy.order.entity.*;
import com.pharmacy.order.enums.OrderStatus;
import com.pharmacy.order.repository.CartRepository;
import com.pharmacy.order.repository.OrderRepository;
import com.pharmacy.order.repository.AddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private CatalogClient catalogClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OrderService orderService;

    private Cart testCart;
    private Address testAddress;
    private MedicineInfoDTO testMedicine;

    @BeforeEach
    void setUp() {
        testAddress = new Address();
        testAddress.setId(1L);
        testAddress.setFullName("John Doe");
        testAddress.setCity("New York");
        testAddress.setState("NY");
        testAddress.setPincode("10001");

        testMedicine = new MedicineInfoDTO();
        testMedicine.setId(1L);
        testMedicine.setName("Aspirin");
        testMedicine.setPrice(BigDecimal.valueOf(10.00));
        testMedicine.setStock(100);
        testMedicine.setRequiresPrescription(false);

        CartItem cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setMedicineId(1L);
        cartItem.setMedicineName("Aspirin");
        cartItem.setQuantity(2);
        cartItem.setPrice(BigDecimal.valueOf(10.00));
        cartItem.setRequiresPrescription(false);

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUserEmail("john@example.com");
        testCart.setItems(new ArrayList<>());
        testCart.getItems().add(cartItem);
    }

    @Test
    void checkout_Success() {
        CheckoutRequest request = new CheckoutRequest();
        request.setAddressId(1L);
        request.setDeliverySlot("Morning");

        when(cartRepository.findByUserEmail(anyString())).thenReturn(Optional.of(testCart));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(testAddress));
        when(catalogClient.getMedicineById(anyLong())).thenReturn(testMedicine);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        OrderResponse response = orderService.checkout("john@example.com", request);

        assertNotNull(response);
        assertEquals(OrderStatus.PAYMENT_PENDING, response.getStatus());
        verify(orderRepository, atLeast(1)).save(any(Order.class));
        verify(cartRepository).save(any(Cart.class));
        verify(catalogClient, never()).assignPrescriptionOrder(anyLong(), anyLong());
    }

    @Test
    void checkout_ShouldLinkPrescriptionToSavedOrder() {
        CartItem rxItem = new CartItem();
        rxItem.setId(2L);
        rxItem.setMedicineId(2L);
        rxItem.setMedicineName("Rx Medicine");
        rxItem.setQuantity(1);
        rxItem.setPrice(BigDecimal.valueOf(50.00));
        rxItem.setRequiresPrescription(true);
        testCart.getItems().add(rxItem);

        MedicineInfoDTO rxMedicine = new MedicineInfoDTO();
        rxMedicine.setId(2L);
        rxMedicine.setName("Rx Medicine");
        rxMedicine.setPrice(BigDecimal.valueOf(50.00));
        rxMedicine.setStock(10);
        rxMedicine.setRequiresPrescription(true);

        CheckoutRequest request = new CheckoutRequest();
        request.setAddressId(1L);
        request.setDeliverySlot("Evening");
        request.setPrescriptionId(103L);

        when(cartRepository.findByUserEmail(anyString())).thenReturn(Optional.of(testCart));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(testAddress));
        when(catalogClient.getMedicineById(1L)).thenReturn(testMedicine);
        when(catalogClient.getMedicineById(2L)).thenReturn(rxMedicine);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getId() == null) {
                order.setId(5012L);
            }
            return order;
        });
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        OrderResponse response = orderService.checkout("john@example.com", request);

        assertNotNull(response);
        assertEquals(OrderStatus.PRESCRIPTION_PENDING, response.getStatus());
        verify(catalogClient).assignPrescriptionOrder(103L, 5012L);
    }

    @Test
    void checkout_CartEmpty() {
        testCart.getItems().clear();
        when(cartRepository.findByUserEmail(anyString())).thenReturn(Optional.of(testCart));

        CheckoutRequest request = new CheckoutRequest();
        request.setAddressId(1L);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> orderService.checkout("john@example.com", request));

        assertTrue(exception.getMessage().contains("Cart is empty"));
    }

    @Test
    void checkout_CartNotFound() {
        when(cartRepository.findByUserEmail(anyString())).thenReturn(Optional.empty());

        CheckoutRequest request = new CheckoutRequest();
        request.setAddressId(1L);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> orderService.checkout("john@example.com", request));

        assertTrue(exception.getMessage().contains("Cart is empty"));
    }

    @Test
    void checkout_AddressNotFound() {
        when(cartRepository.findByUserEmail(anyString())).thenReturn(Optional.of(testCart));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.empty());

        CheckoutRequest request = new CheckoutRequest();
        request.setAddressId(999L);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> orderService.checkout("john@example.com", request));

        assertTrue(exception.getMessage().contains("Address not found"));
    }

    @Test
    void checkout_PrescriptionRequired() {
        CartItem rxItem = new CartItem();
        rxItem.setMedicineId(2L);
        rxItem.setRequiresPrescription(true);
        testCart.getItems().add(rxItem);

        when(cartRepository.findByUserEmail(anyString())).thenReturn(Optional.of(testCart));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(testAddress));

        testMedicine.setRequiresPrescription(true);
        when(catalogClient.getMedicineById(anyLong())).thenReturn(testMedicine);

        CheckoutRequest request = new CheckoutRequest();
        request.setAddressId(1L);
        request.setPrescriptionId(null);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> orderService.checkout("john@example.com", request));

        assertTrue(exception.getMessage().contains("Prescription"));
    }

    @Test
    void checkout_InsufficientStock() {
        testMedicine.setStock(1);
        when(cartRepository.findByUserEmail(anyString())).thenReturn(Optional.of(testCart));
        when(addressRepository.findById(anyLong())).thenReturn(Optional.of(testAddress));
        when(catalogClient.getMedicineById(anyLong())).thenReturn(testMedicine);

        CheckoutRequest request = new CheckoutRequest();
        request.setAddressId(1L);

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> orderService.checkout("john@example.com", request));

        assertTrue(exception.getMessage().contains("Insufficient stock"));
    }

    @Test
    void getMyOrders_Success() {
        Order order = new Order();
        order.setId(1L);
        order.setUserEmail("john@example.com");
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        order.setTotalAmount(BigDecimal.valueOf(100.00));

        when(orderRepository.findByUserEmailOrderByCreatedAtDesc(anyString()))
            .thenReturn(List.of(order));

        List<OrderResponse> orders = orderService.getMyOrders("john@example.com");

        assertNotNull(orders);
        assertEquals(1, orders.size());
        assertEquals(OrderStatus.PAYMENT_PENDING, orders.get(0).getStatus());
    }

    @Test
    void getOrderById_Success() {
        Order order = new Order();
        order.setId(1L);
        order.setUserEmail("john@example.com");
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        order.setTotalAmount(BigDecimal.valueOf(100.00));
        order.setDeliveryAddress(testAddress);
        order.setItems(new ArrayList<>());

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderById(1L, "john@example.com");

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void getOrderById_Unauthorized() {
        Order order = new Order();
        order.setId(1L);
        order.setUserEmail("john@example.com");

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> orderService.getOrderById(1L, "other@example.com"));

        assertTrue(exception.getMessage().contains("Unauthorized"));
    }

    @Test
    void getOrderById_NotFound() {
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> orderService.getOrderById(999L, "john@example.com"));

        assertTrue(exception.getMessage().contains("Order not found"));
    }

    @Test
    void cancelOrder_Success() {
        Order order = new Order();
        order.setId(1L);
        order.setUserEmail("john@example.com");
        order.setStatus(OrderStatus.PAYMENT_PENDING);

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse response = orderService.cancelOrder(1L, "john@example.com");

        assertNotNull(response);
        assertEquals(OrderStatus.CUSTOMER_CANCELLED, response.getStatus());
    }

    @Test
    void cancelOrder_AlreadyDelivered() {
        Order order = new Order();
        order.setId(1L);
        order.setUserEmail("john@example.com");
        order.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> orderService.cancelOrder(1L, "john@example.com"));

        assertTrue(exception.getMessage().contains("cannot be cancelled"));
    }

    @Test
    void processPayment_Success() {
        Order order = new Order();
        order.setId(1L);
        order.setUserEmail("john@example.com");
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        order.setTotalAmount(BigDecimal.valueOf(100.00));

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        PaymentRequest request = new PaymentRequest();
        request.setOrderId(1L);
        request.setPaymentMethod("CREDIT_CARD");

        PaymentResponse response = orderService.processPayment(request, "john@example.com");

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(OrderStatus.PAID, response.getOrderStatus());
    }

    @Test
    void processPayment_Failed() {
        Order order = new Order();
        order.setId(1L);
        order.setUserEmail("john@example.com");
        order.setStatus(OrderStatus.PAYMENT_PENDING);

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        PaymentRequest request = new PaymentRequest();
        request.setOrderId(1L);
        request.setPaymentMethod("FAIL");

        PaymentResponse response = orderService.processPayment(request, "john@example.com");

        assertNotNull(response);
        assertEquals("FAILED", response.getStatus());
        assertEquals(OrderStatus.PAYMENT_FAILED, response.getOrderStatus());
    }

    @Test
    void updateOrderStatus_ValidTransition() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PAID);

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse response = orderService.updateOrderStatus(1L, OrderStatus.PACKED);

        assertNotNull(response);
        assertEquals(OrderStatus.PACKED, response.getStatus());
    }

    @Test
    void updateOrderStatus_InvalidTransition() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PAYMENT_PENDING);

        when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> orderService.updateOrderStatus(1L, OrderStatus.PACKED));

        assertTrue(exception.getMessage().contains("Invalid transition"));
    }
}
