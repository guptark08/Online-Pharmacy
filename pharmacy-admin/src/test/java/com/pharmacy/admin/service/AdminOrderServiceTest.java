package com.pharmacy.admin.service;

import com.pharmacy.admin.dto.OrderStatusRequest;
import com.pharmacy.admin.dto.OrderSummaryDTO;
import com.pharmacy.admin.enums.OrderStatus;
import com.pharmacy.admin.repository.PrescriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminOrderServiceTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement selectAllStatement;

    @Mock
    private PreparedStatement updateStatement;

    @Mock
    private PreparedStatement selectOneStatement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Spy
    @InjectMocks
    private AdminOrderService adminOrderService;

    @Test
    void getAllOrdersReturnsMappedRows() throws Exception {
        Timestamp createdAt = Timestamp.valueOf(LocalDateTime.now());

        doReturn(connection).when(adminOrderService).getConnection();
        when(connection.prepareStatement(anyString())).thenReturn(selectAllStatement);
        when(selectAllStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(1L, 2L);
        when(resultSet.getString("user_email")).thenReturn("one@example.com", "two@example.com");
        when(resultSet.getString("status")).thenReturn("PAYMENT_PENDING", "DELIVERED");
        when(resultSet.getBigDecimal("total_amount"))
            .thenReturn(BigDecimal.TEN, BigDecimal.valueOf(20));
        when(resultSet.getTimestamp("created_at")).thenReturn(createdAt, createdAt);

        List<OrderSummaryDTO> orders = adminOrderService.getAllOrders();

        assertEquals(2, orders.size());
        assertEquals(OrderStatus.PAYMENT_PENDING, orders.get(0).getStatus());
        assertEquals(OrderStatus.DELIVERED, orders.get(1).getStatus());
    }

    @Test
    void getAllOrdersWrapsSqlErrors() throws Exception {
        doThrow(new SQLException("boom")).when(adminOrderService).getConnection();

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> adminOrderService.getAllOrders());

        assertTrue(ex.getMessage().contains("Error fetching orders"));
    }

    @Test
    void updateOrderStatusReturnsUpdatedOrder() throws Exception {
        Timestamp createdAt = Timestamp.valueOf(LocalDateTime.now());
        OrderStatusRequest request = new OrderStatusRequest();
        request.setStatus(OrderStatus.PACKED.name());

        doReturn(connection).when(adminOrderService).getConnection();
        when(connection.prepareStatement(anyString()))
            .thenReturn(updateStatement, selectOneStatement);
        when(updateStatement.executeUpdate()).thenReturn(1);
        when(selectOneStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("user_email")).thenReturn("user@example.com");
        when(resultSet.getString("status")).thenReturn("PACKED");
        when(resultSet.getBigDecimal("total_amount")).thenReturn(BigDecimal.valueOf(50));
        when(resultSet.getTimestamp("created_at")).thenReturn(createdAt);

        OrderSummaryDTO updated = adminOrderService.updateOrderStatus(1L, request);

        assertEquals(OrderStatus.PACKED, updated.getStatus());
        verify(updateStatement).setString(1, "PACKED");
        verify(updateStatement).setLong(2, 1L);
    }

    @Test
    void updateOrderStatusThrowsWhenOrderMissingAfterUpdate() throws Exception {
        OrderStatusRequest request = new OrderStatusRequest();
        request.setStatus(OrderStatus.PACKED.name());

        doReturn(connection).when(adminOrderService).getConnection();
        when(connection.prepareStatement(anyString()))
            .thenReturn(updateStatement, selectOneStatement);
        when(updateStatement.executeUpdate()).thenReturn(1);
        when(selectOneStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> adminOrderService.updateOrderStatus(99L, request));

        assertTrue(ex.getMessage().contains("Order not found"));
    }
}
