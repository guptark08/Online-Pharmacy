package com.pharmacy.order;

import com.pharmacy.order.controller.AddressController;
import com.pharmacy.order.controller.CartController;
import com.pharmacy.order.controller.OrderController;
import com.pharmacy.order.dto.AddressDTO;
import com.pharmacy.order.dto.CartDTO;
import com.pharmacy.order.dto.CartItemDTO;
import com.pharmacy.order.dto.CartItemRequest;
import com.pharmacy.order.dto.CheckoutRequest;
import com.pharmacy.order.dto.MedicineInfoDTO;
import com.pharmacy.order.dto.OrderEvent;
import com.pharmacy.order.dto.OrderItemDTO;
import com.pharmacy.order.dto.OrderResponse;
import com.pharmacy.order.dto.OrderStatusUpdateRequest;
import com.pharmacy.order.dto.PaymentRequest;
import com.pharmacy.order.dto.PaymentResponse;
import com.pharmacy.order.entity.Address;
import com.pharmacy.order.entity.Cart;
import com.pharmacy.order.entity.CartItem;
import com.pharmacy.order.entity.Order;
import com.pharmacy.order.entity.OrderItem;
import com.pharmacy.order.entity.Payment;
import com.pharmacy.order.entity.User;
import com.pharmacy.order.enums.OrderStatus;
import com.pharmacy.order.enums.PaymentStatus;
import com.pharmacy.order.enums.Role;
import com.pharmacy.order.exception.GlobalExceptionHandler;
import com.pharmacy.order.security.JwtAuthFilter;
import com.pharmacy.order.security.JwtUtil;
import com.pharmacy.order.service.AddressService;
import com.pharmacy.order.service.CartService;
import com.pharmacy.order.service.OrderService;
import com.pharmacy.order.service.UserDetailsServiceImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderCoverageSupportTest {

    private static final String SECRET =
        "cGhhcm1hY3ktc3VwZXItc2VjcmV0LWtleS0yMDI0LXNwcmluZy1ib290LWp3dA==";

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void main_ShouldDelegateToSpringApplication() {
        String[] args = {"--test"};

        try (MockedStatic<SpringApplication> springApplication =
                     mockStatic(SpringApplication.class)) {
            PharmacyOrderApplication.main(args);

            springApplication.verify(() ->
                SpringApplication.run(PharmacyOrderApplication.class, args));
        }
    }

    @Test
    void controllers_ShouldDelegateToServices() {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        when(jwtUtil.extractUsername("token")).thenReturn("user@pharmacy.com");

        CartService cartService = mock(CartService.class);
        CartController cartController = new CartController();
        ReflectionTestUtils.setField(cartController, "cartService", cartService);
        ReflectionTestUtils.setField(cartController, "jwtUtil", jwtUtil);

        CartDTO cart = new CartDTO();
        when(cartService.getCart("user@pharmacy.com")).thenReturn(cart);
        when(cartService.addItem(any(String.class), any(CartItemRequest.class))).thenReturn(cart);
        when(cartService.updateItem("user@pharmacy.com", 2L, 3)).thenReturn(cart);
        when(cartService.removeItem("user@pharmacy.com", 2L)).thenReturn(cart);

        assertThat(cartController.getCart("Bearer token").getBody()).isSameAs(cart);
        assertThat(cartController.addItem("Bearer token", new CartItemRequest()).getBody()).isSameAs(cart);
        assertThat(cartController.updateItem("Bearer token", 2L, 3).getBody()).isSameAs(cart);
        assertThat(cartController.removeItem("Bearer token", 2L).getBody()).isSameAs(cart);
        assertThat(cartController.clearCart("Bearer token").getBody()).isEqualTo("Cart cleared");
        verify(cartService).clearCart("user@pharmacy.com");

        AddressService addressService = mock(AddressService.class);
        AddressController addressController = new AddressController();
        ReflectionTestUtils.setField(addressController, "addressService", addressService);
        ReflectionTestUtils.setField(addressController, "jwtUtil", jwtUtil);

        AddressDTO addressDTO = new AddressDTO();
        when(addressService.saveAddress("user@pharmacy.com", addressDTO)).thenReturn(addressDTO);
        when(addressService.getMyAddresses("user@pharmacy.com")).thenReturn(List.of(addressDTO));

        assertThat(addressController.saveAddress("Bearer token", addressDTO).getBody()).isSameAs(addressDTO);
        assertThat(addressController.getMyAddresses("Bearer token").getBody()).hasSize(1);

        OrderService orderService = mock(OrderService.class);
        OrderController orderController = new OrderController();
        ReflectionTestUtils.setField(orderController, "orderService", orderService);
        ReflectionTestUtils.setField(orderController, "jwtUtil", jwtUtil);

        CheckoutRequest checkoutRequest = new CheckoutRequest();
        OrderResponse orderResponse = new OrderResponse();
        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setStatus("PAID");
        when(orderService.checkout("user@pharmacy.com", checkoutRequest)).thenReturn(orderResponse);
        when(orderService.getMyOrders("user@pharmacy.com")).thenReturn(List.of(orderResponse));
        when(orderService.getOrderById(9L, "user@pharmacy.com")).thenReturn(orderResponse);
        when(orderService.cancelOrder(9L, "user@pharmacy.com")).thenReturn(orderResponse);
        when(orderService.updateOrderStatus(9L, OrderStatus.PACKED)).thenReturn(orderResponse);
        when(orderService.processPayment(any(PaymentRequest.class), any(String.class))).thenReturn(paymentResponse);

        assertThat(orderController.checkout("Bearer token", checkoutRequest).getBody()).isSameAs(orderResponse);
        assertThat(orderController.getMyOrders("Bearer token").getBody()).hasSize(1);
        assertThat(orderController.getOrderById("Bearer token", 9L).getBody()).isSameAs(orderResponse);
        assertThat(orderController.cancelOrder("Bearer token", 9L).getBody()).isSameAs(orderResponse);

        OrderStatusUpdateRequest statusRequest = new OrderStatusUpdateRequest();
        statusRequest.setStatus(OrderStatus.PACKED);
        assertThat(orderController.updateStatus("Bearer token", 9L, statusRequest).getBody())
            .isSameAs(orderResponse);

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentMethod("CARD");
        paymentRequest.setTransactionReference("TXN-1");
        assertThat(orderController.payOrder("Bearer token", 9L, paymentRequest).getBody())
            .isSameAs(paymentResponse);

        ArgumentCaptor<PaymentRequest> captor = ArgumentCaptor.forClass(PaymentRequest.class);
        verify(orderService).processPayment(captor.capture(), any(String.class));
        assertThat(captor.getValue().getOrderId()).isEqualTo(9L);
    }

    @Test
    void jwtUtil_ShouldExtractAndValidateToken() {
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 60_000L);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
            .withUsername("user@pharmacy.com")
            .password("password")
            .authorities("ROLE_CUSTOMER")
            .build();

        UserDetails differentUser = org.springframework.security.core.userdetails.User
            .withUsername("other@pharmacy.com")
            .password("password")
            .authorities("ROLE_CUSTOMER")
            .build();

        String token = jwtUtil.generateToken(userDetails);
        String subject = jwtUtil.extractClaim(token, claims -> claims.getSubject());

        assertThat(jwtUtil.extractUsername(token)).isEqualTo("user@pharmacy.com");
        assertThat(jwtUtil.extractExpiration(token)).isAfter(new Date());
        assertThat(subject).isEqualTo("user@pharmacy.com");
        assertThat(jwtUtil.validateToken(token, userDetails)).isTrue();
        assertThat(jwtUtil.validateToken(token, differentUser)).isFalse();

        String manualToken = createToken("user@pharmacy.com",
            new Date(System.currentTimeMillis() + 60_000));
        assertThat(jwtUtil.extractUsername(manualToken)).isEqualTo("user@pharmacy.com");
    }

    @Test
    void jwtAuthFilter_ShouldHandleMissingInvalidAndValidTokens() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        UserDetailsServiceImpl userDetailsService = mock(UserDetailsServiceImpl.class);
        JwtAuthFilter filter = new JwtAuthFilter();
        ReflectionTestUtils.setField(filter, "jwtUtil", jwtUtil);
        ReflectionTestUtils.setField(filter, "userDetailsService", userDetailsService);

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(),
            new MockFilterChain());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        MockHttpServletRequest invalidRequest = new MockHttpServletRequest();
        invalidRequest.addHeader("Authorization", "Bearer broken");
        when(jwtUtil.extractUsername("broken")).thenThrow(new IllegalArgumentException("bad"));
        filter.doFilter(invalidRequest, new MockHttpServletResponse(), new MockFilterChain());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        MockHttpServletRequest validRequest = new MockHttpServletRequest();
        validRequest.addHeader("Authorization", "Bearer valid");
        UserDetails userDetails = org.springframework.security.core.userdetails.User
            .withUsername("user@pharmacy.com")
            .password("password")
            .authorities("ROLE_CUSTOMER")
            .build();
        when(jwtUtil.extractUsername("valid")).thenReturn("user@pharmacy.com");
        when(userDetailsService.loadUserByUsername("user@pharmacy.com")).thenReturn(userDetails);
        when(jwtUtil.validateToken("valid", userDetails)).thenReturn(true);

        filter.doFilter(validRequest, new MockHttpServletResponse(), new MockFilterChain());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("existing", null, List.of()));
        filter.doFilter(validRequest, new MockHttpServletResponse(), new MockFilterChain());
        verify(jwtUtil, atLeastOnce()).extractUsername("valid");
    }

    @Test
    void globalExceptionHandler_ShouldBuildExpectedResponses() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        Map<String, Object> runtimeBody = handler.handleRuntime(
            new RuntimeException("bad request")).getBody();
        Map<String, Object> generalBody = handler.handleGeneral(
            new Exception("general")).getBody();

        assertThat(runtimeBody.get("status")).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(runtimeBody.get("message")).isEqualTo("bad request");
        assertThat(generalBody.get("status")).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void dtoAndEntityTypes_ShouldStoreValues() {
        LocalDateTime now = LocalDateTime.now();

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setId(1L);
        addressDTO.setFullName("User");
        addressDTO.setMobile("9999999999");
        addressDTO.setAddressLine1("A1");
        addressDTO.setAddressLine2("A2");
        addressDTO.setCity("Ara");
        addressDTO.setState("Bihar");
        addressDTO.setPincode("802301");
        addressDTO.setDefault(true);

        CartItemDTO cartItemDTO = new CartItemDTO();
        cartItemDTO.setId(2L);
        cartItemDTO.setMedicineId(101L);
        cartItemDTO.setMedicineName("Paracetamol");
        cartItemDTO.setPrice(BigDecimal.TEN);
        cartItemDTO.setQuantity(2);
        cartItemDTO.setSubtotal(BigDecimal.valueOf(20));
        cartItemDTO.setRequiresPrescription(false);

        CartDTO cartDTO = new CartDTO();
        cartDTO.setId(3L);
        cartDTO.setUserId(4L);
        cartDTO.setItems(List.of(cartItemDTO));
        cartDTO.setTotalAmount(BigDecimal.valueOf(20));
        cartDTO.setTotalItems(1);

        OrderItemDTO orderItemDTO = new OrderItemDTO();
        orderItemDTO.setId(5L);
        orderItemDTO.setMedicineId(101L);
        orderItemDTO.setMedicineName("Paracetamol");
        orderItemDTO.setPrice(BigDecimal.TEN);
        orderItemDTO.setQuantity(1);
        orderItemDTO.setSubtotal(BigDecimal.TEN);

        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setId(6L);
        orderResponse.setUserId(7L);
        orderResponse.setItems(List.of(orderItemDTO));
        orderResponse.setDeliveryAddress(addressDTO);
        orderResponse.setPrescriptionId(8L);
        orderResponse.setStatus(OrderStatus.PAID);
        orderResponse.setTotalAmount(BigDecimal.TEN);
        orderResponse.setDeliverySlot("10AM-12PM");
        orderResponse.setCreatedAt(now);

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(9L);
        paymentRequest.setPaymentMethod("CARD");
        paymentRequest.setTransactionReference("TXN-1");

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setOrderId(9L);
        paymentResponse.setStatus("PAID");
        paymentResponse.setOrderStatus(OrderStatus.PAID);
        paymentResponse.setMessage("done");

        OrderStatusUpdateRequest statusUpdateRequest = new OrderStatusUpdateRequest();
        statusUpdateRequest.setStatus(OrderStatus.OUT_FOR_DELIVERY);

        OrderEvent emptyOrderEvent = new OrderEvent();
        OrderEvent orderEvent = new OrderEvent(10L, "user@pharmacy.com", OrderStatus.PACKED);
        orderEvent.setOrderId(10L);
        orderEvent.setUserEmail("user@pharmacy.com");
        orderEvent.setStatus(OrderStatus.OUT_FOR_DELIVERY);

        Payment payment = new Payment();
        payment.setId(11L);
        payment.setOrderId(9L);
        payment.setUserId(7L);
        payment.setAmount(BigDecimal.TEN);
        payment.setStatus(PaymentStatus.PAID);
        payment.setTransactionId("TXN-1");
        payment.setPaymentMethod("UPI");
        ReflectionTestUtils.setField(payment, "createdAt", now.plusMinutes(1));

        User user = new User();
        user.setId(12L);
        user.setName("Order User");
        user.setEmail("user@pharmacy.com");
        user.setMobile("9999999999");
        user.setPassword("secret");
        user.setRole(Role.CUSTOMER);
        user.setActive(true);
        ReflectionTestUtils.setField(user, "createdAt", now.plusMinutes(2));

        CartItemRequest cartItemRequest = new CartItemRequest();
        cartItemRequest.setMedicineId(101L);
        cartItemRequest.setMedicineName("Paracetamol");
        cartItemRequest.setPrice(BigDecimal.TEN);
        cartItemRequest.setQuantity(2);
        cartItemRequest.setRequiresPrescription(false);

        MedicineInfoDTO medicineInfoDTO = new MedicineInfoDTO();
        medicineInfoDTO.setId(13L);
        medicineInfoDTO.setName("Paracetamol");
        medicineInfoDTO.setPrice(BigDecimal.TEN);
        medicineInfoDTO.setRequiresPrescription(false);
        medicineInfoDTO.setStock(15);

        Address address = new Address();
        address.setId(14L);
        address.setUserEmail("user@pharmacy.com");
        address.setFullName("Home User");
        address.setMobile("8888888888");
        address.setAddressLine1("Street 1");
        address.setAddressLine2("Street 2");
        address.setCity("Ara");
        address.setState("Bihar");
        address.setPincode("802301");
        address.setDefault(true);
        ReflectionTestUtils.setField(address, "createdAt", now.plusMinutes(3));

        Cart cart = new Cart();
        cart.setId(15L);
        cart.setUserEmail("user@pharmacy.com");
        ReflectionTestUtils.setField(cart, "updatedAt", now.plusMinutes(4));

        CartItem cartItem = new CartItem();
        cartItem.setId(16L);
        cartItem.setCart(cart);
        cartItem.setMedicineId(101L);
        cartItem.setMedicineName("Paracetamol");
        cartItem.setPrice(BigDecimal.TEN);
        cartItem.setQuantity(2);
        cartItem.setRequiresPrescription(false);
        cart.setItems(List.of(cartItem));

        Order order = new Order();
        order.setId(17L);
        order.setUserEmail("user@pharmacy.com");
        order.setDeliveryAddress(address);
        order.setPrescriptionId(18L);
        order.setStatus(OrderStatus.PAID);
        order.setTotalAmount(BigDecimal.valueOf(25));
        order.setDeliverySlot("2PM-4PM");
        ReflectionTestUtils.setField(order, "createdAt", now.plusMinutes(5));
        ReflectionTestUtils.setField(order, "updatedAt", now.plusMinutes(6));

        OrderItem orderItem = new OrderItem();
        orderItem.setId(19L);
        orderItem.setOrder(order);
        orderItem.setMedicineId(101L);
        orderItem.setMedicineName("Paracetamol");
        orderItem.setPrice(BigDecimal.TEN);
        orderItem.setQuantity(2);
        orderItem.setSubtotal(BigDecimal.valueOf(20));
        order.setItems(List.of(orderItem));

        assertThat(addressDTO.getId()).isEqualTo(1L);
        assertThat(addressDTO.getFullName()).isEqualTo("User");
        assertThat(addressDTO.getMobile()).isEqualTo("9999999999");
        assertThat(addressDTO.getAddressLine1()).isEqualTo("A1");
        assertThat(addressDTO.getAddressLine2()).isEqualTo("A2");
        assertThat(addressDTO.getCity()).isEqualTo("Ara");
        assertThat(addressDTO.getState()).isEqualTo("Bihar");
        assertThat(addressDTO.getPincode()).isEqualTo("802301");
        assertThat(addressDTO.isDefault()).isTrue();

        assertThat(cartItemDTO.getId()).isEqualTo(2L);
        assertThat(cartItemDTO.getMedicineId()).isEqualTo(101L);
        assertThat(cartItemDTO.getMedicineName()).isEqualTo("Paracetamol");
        assertThat(cartItemDTO.getPrice()).isEqualTo(BigDecimal.TEN);
        assertThat(cartItemDTO.getQuantity()).isEqualTo(2);
        assertThat(cartItemDTO.getSubtotal()).isEqualTo(BigDecimal.valueOf(20));
        assertThat(cartItemDTO.isRequiresPrescription()).isFalse();

        assertThat(cartDTO.getId()).isEqualTo(3L);
        assertThat(cartDTO.getUserId()).isEqualTo(4L);
        assertThat(cartDTO.getItems()).containsExactly(cartItemDTO);
        assertThat(cartDTO.getTotalAmount()).isEqualTo(BigDecimal.valueOf(20));
        assertThat(cartDTO.getTotalItems()).isEqualTo(1);

        assertThat(orderItemDTO.getId()).isEqualTo(5L);
        assertThat(orderItemDTO.getMedicineId()).isEqualTo(101L);
        assertThat(orderItemDTO.getMedicineName()).isEqualTo("Paracetamol");
        assertThat(orderItemDTO.getPrice()).isEqualTo(BigDecimal.TEN);
        assertThat(orderItemDTO.getQuantity()).isEqualTo(1);
        assertThat(orderItemDTO.getSubtotal()).isEqualTo(BigDecimal.TEN);

        assertThat(orderResponse.getId()).isEqualTo(6L);
        assertThat(orderResponse.getUserId()).isEqualTo(7L);
        assertThat(orderResponse.getItems()).containsExactly(orderItemDTO);
        assertThat(orderResponse.getDeliveryAddress()).isSameAs(addressDTO);
        assertThat(orderResponse.getPrescriptionId()).isEqualTo(8L);
        assertThat(orderResponse.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(orderResponse.getTotalAmount()).isEqualTo(BigDecimal.TEN);
        assertThat(orderResponse.getDeliverySlot()).isEqualTo("10AM-12PM");
        assertThat(orderResponse.getCreatedAt()).isEqualTo(now);

        assertThat(paymentRequest.getOrderId()).isEqualTo(9L);
        assertThat(paymentRequest.getPaymentMethod()).isEqualTo("CARD");
        assertThat(paymentRequest.getTransactionReference()).isEqualTo("TXN-1");
        assertThat(paymentResponse.getOrderId()).isEqualTo(9L);
        assertThat(paymentResponse.getStatus()).isEqualTo("PAID");
        assertThat(paymentResponse.getOrderStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(paymentResponse.getMessage()).isEqualTo("done");
        assertThat(statusUpdateRequest.getStatus()).isEqualTo(OrderStatus.OUT_FOR_DELIVERY);
        assertThat(emptyOrderEvent.getOrderId()).isNull();
        assertThat(emptyOrderEvent.getUserEmail()).isNull();
        assertThat(emptyOrderEvent.getStatus()).isNull();
        assertThat(orderEvent.getOrderId()).isEqualTo(10L);
        assertThat(orderEvent.getUserEmail()).isEqualTo("user@pharmacy.com");
        assertThat(orderEvent.getStatus()).isEqualTo(OrderStatus.OUT_FOR_DELIVERY);
        assertThat(payment.getId()).isEqualTo(11L);
        assertThat(payment.getOrderId()).isEqualTo(9L);
        assertThat(payment.getUserId()).isEqualTo(7L);
        assertThat(payment.getAmount()).isEqualTo(BigDecimal.TEN);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getTransactionId()).isEqualTo("TXN-1");
        assertThat(payment.getPaymentMethod()).isEqualTo("UPI");
        assertThat(payment.getCreatedAt()).isEqualTo(now.plusMinutes(1));
        assertThat(user.getId()).isEqualTo(12L);
        assertThat(user.getName()).isEqualTo("Order User");
        assertThat(user.getEmail()).isEqualTo("user@pharmacy.com");
        assertThat(user.getMobile()).isEqualTo("9999999999");
        assertThat(user.getPassword()).isEqualTo("secret");
        assertThat(user.getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(user.isActive()).isTrue();
        assertThat(user.getCreatedAt()).isEqualTo(now.plusMinutes(2));
        assertThat(medicineInfoDTO.getId()).isEqualTo(13L);
        assertThat(medicineInfoDTO.getName()).isEqualTo("Paracetamol");
        assertThat(medicineInfoDTO.getPrice()).isEqualTo(BigDecimal.TEN);
        assertThat(medicineInfoDTO.isRequiresPrescription()).isFalse();
        assertThat(medicineInfoDTO.getStock()).isEqualTo(15);
        assertThat(address.getId()).isEqualTo(14L);
        assertThat(address.getUserEmail()).isEqualTo("user@pharmacy.com");
        assertThat(address.getFullName()).isEqualTo("Home User");
        assertThat(address.getMobile()).isEqualTo("8888888888");
        assertThat(address.getAddressLine1()).isEqualTo("Street 1");
        assertThat(address.getAddressLine2()).isEqualTo("Street 2");
        assertThat(address.getCity()).isEqualTo("Ara");
        assertThat(address.getState()).isEqualTo("Bihar");
        assertThat(address.getPincode()).isEqualTo("802301");
        assertThat(address.isDefault()).isTrue();
        assertThat(address.getCreatedAt()).isEqualTo(now.plusMinutes(3));
        assertThat(cart.getId()).isEqualTo(15L);
        assertThat(cart.getUserEmail()).isEqualTo("user@pharmacy.com");
        assertThat(cart.getItems()).containsExactly(cartItem);
        assertThat(cart.getUpdatedAt()).isEqualTo(now.plusMinutes(4));
        assertThat(cartItem.getId()).isEqualTo(16L);
        assertThat(cartItem.getCart()).isSameAs(cart);
        assertThat(cartItem.getMedicineId()).isEqualTo(101L);
        assertThat(cartItem.getMedicineName()).isEqualTo("Paracetamol");
        assertThat(cartItem.getPrice()).isEqualTo(BigDecimal.TEN);
        assertThat(cartItem.getQuantity()).isEqualTo(2);
        assertThat(cartItem.isRequiresPrescription()).isFalse();
        assertThat(order.getId()).isEqualTo(17L);
        assertThat(order.getUserEmail()).isEqualTo("user@pharmacy.com");
        assertThat(order.getItems()).containsExactly(orderItem);
        assertThat(order.getDeliveryAddress()).isSameAs(address);
        assertThat(order.getPrescriptionId()).isEqualTo(18L);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getTotalAmount()).isEqualTo(BigDecimal.valueOf(25));
        assertThat(order.getDeliverySlot()).isEqualTo("2PM-4PM");
        assertThat(order.getCreatedAt()).isEqualTo(now.plusMinutes(5));
        assertThat(order.getUpdatedAt()).isEqualTo(now.plusMinutes(6));
        assertThat(orderItem.getId()).isEqualTo(19L);
        assertThat(orderItem.getOrder()).isSameAs(order);
        assertThat(orderItem.getMedicineId()).isEqualTo(101L);
        assertThat(orderItem.getMedicineName()).isEqualTo("Paracetamol");
        assertThat(orderItem.getPrice()).isEqualTo(BigDecimal.TEN);
        assertThat(orderItem.getQuantity()).isEqualTo(2);
        assertThat(orderItem.getSubtotal()).isEqualTo(BigDecimal.valueOf(20));
        assertThat(cartItemRequest.getMedicineId()).isEqualTo(101L);
        assertThat(cartItemRequest.getMedicineName()).isEqualTo("Paracetamol");
        assertThat(cartItemRequest.getPrice()).isEqualTo(BigDecimal.TEN);
        assertThat(cartItemRequest.getQuantity()).isEqualTo(2);
        assertThat(cartItemRequest.isRequiresPrescription()).isFalse();
    }

    private String createToken(String subject, Date expiration) {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Jwts.builder()
            .subject(subject)
            .issuedAt(new Date())
            .expiration(expiration)
            .signWith(Keys.hmacShaKeyFor(keyBytes))
            .compact();
    }
}
