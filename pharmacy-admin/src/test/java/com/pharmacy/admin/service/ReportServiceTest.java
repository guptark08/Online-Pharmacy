package com.pharmacy.admin.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement revenueStatement;

    @Mock
    private PreparedStatement totalOrdersStatement;

    @Mock
    private PreparedStatement byStatusStatement;

    @Mock
    private ResultSet revenueResult;

    @Mock
    private ResultSet totalOrdersResult;

    @Mock
    private ResultSet byStatusResult;

    @Spy
    @InjectMocks
    private ReportService reportService;

    @Test
    void getSalesReportReturnsAggregates() throws Exception {
        doReturn(connection).when(reportService).getConnection();
        when(connection.prepareStatement(anyString()))
            .thenReturn(revenueStatement, totalOrdersStatement, byStatusStatement);
        when(revenueStatement.executeQuery()).thenReturn(revenueResult);
        when(totalOrdersStatement.executeQuery()).thenReturn(totalOrdersResult);
        when(byStatusStatement.executeQuery()).thenReturn(byStatusResult);
        when(revenueResult.next()).thenReturn(true);
        when(revenueResult.getBigDecimal(1)).thenReturn(BigDecimal.valueOf(90));
        when(totalOrdersResult.next()).thenReturn(true);
        when(totalOrdersResult.getLong(1)).thenReturn(4L);
        when(byStatusResult.next()).thenReturn(true, false);
        when(byStatusResult.getString("status")).thenReturn("PAID");
        when(byStatusResult.getLong("count")).thenReturn(4L);

        Map<String, Object> report = reportService.getSalesReport();

        assertEquals(BigDecimal.valueOf(90), report.get("totalRevenue"));
        assertEquals(4L, report.get("totalOrders"));
        assertEquals(4L, ((Map<?, ?>) report.get("ordersByStatus")).get("PAID"));
    }

    @Test
    void getSalesReportReturnsErrorOnJdbcFailure() throws Exception {
        doThrow(new RuntimeException("connection failed")).when(reportService).getConnection();

        Map<String, Object> report = reportService.getSalesReport();

        assertTrue(report.containsKey("error"));
    }

    @Test
    void getInventoryReportReturnsMetadata() {
        Map<String, Object> report = reportService.getInventoryReport();

        assertEquals("Inventory Report", report.get("reportType"));
        assertTrue(report.get("generatedAt") instanceof LocalDateTime);
    }
}
