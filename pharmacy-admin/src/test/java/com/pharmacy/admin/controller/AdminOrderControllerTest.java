package com.pharmacy.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.admin.dto.OrderStatusRequest;
import com.pharmacy.admin.dto.OrderSummaryDTO;
import com.pharmacy.admin.enums.OrderStatus;
import com.pharmacy.admin.security.JwtUtil;
import com.pharmacy.admin.service.AdminOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminOrderController.class)
class AdminOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminOrderService adminOrderService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllOrders_ShouldReturnOrderList() throws Exception {
        // Given
        OrderSummaryDTO order1 = new OrderSummaryDTO();
        order1.setId(1L);
        order1.setStatus(OrderStatus.PAYMENT_PENDING);

        OrderSummaryDTO order2 = new OrderSummaryDTO();
        order2.setId(2L);
        order2.setStatus(OrderStatus.DELIVERED);

        when(adminOrderService.getAllOrders()).thenReturn(List.of(order1, order2));

        // When & Then
        mockMvc.perform(get("/api/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateStatus_ShouldReturnUpdatedOrder() throws Exception {
        // Given
        OrderStatusRequest request = new OrderStatusRequest();
        request.setStatus(OrderStatus.PACKED.name());

        OrderSummaryDTO updatedOrder = new OrderSummaryDTO();
        updatedOrder.setId(1L);
        updatedOrder.setStatus(OrderStatus.PACKED);

        when(adminOrderService.updateOrderStatus(eq(1L), any(OrderStatusRequest.class)))
                .thenReturn(updatedOrder);

        // When & Then
        // csrf() zaroori hai — @WebMvcTest mein Spring Security enabled hoti hai
        // PUT/POST/DELETE requests mein CSRF token chahiye hota hai
        mockMvc.perform(put("/api/admin/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PACKED"));
    }
}
