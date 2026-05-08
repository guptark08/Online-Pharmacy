package com.pharmacy.admin.service;

import com.pharmacy.admin.dto.OrderSummaryDTO;
import com.pharmacy.admin.enums.OrderStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ReportService {

    @Value("${order.datasource.url}")
    private String orderDbUrl;

    @Value("${order.datasource.username}")
    private String orderDbUsername;

    @Value("${order.datasource.password}")
    private String orderDbPassword;

    public Map<String, Object> getSalesReport() {
        Map<String, Object> report = new LinkedHashMap<>();

        try (Connection conn = getConnection();
             PreparedStatement ps1 = conn.prepareStatement(
                 "SELECT COALESCE(SUM(total_amount), 0) FROM orders " +
                 "WHERE status = 'PAID'");
             PreparedStatement ps2 = conn.prepareStatement(
                 "SELECT COUNT(*) FROM orders");
             PreparedStatement ps3 = conn.prepareStatement(
                 "SELECT status, COUNT(*) as count FROM orders " +
                 "GROUP BY status")) {
            // Total revenue
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next())
                report.put("totalRevenue", rs1.getBigDecimal(1));

            // Total orders
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next())
                report.put("totalOrders", rs2.getLong(1));

            // Orders by status
            ResultSet rs3 = ps3.executeQuery();
            Map<String, Long> byStatus = new LinkedHashMap<>();
            while (rs3.next()) {
                byStatus.put(rs3.getString("status"), rs3.getLong("count"));
            }
            report.put("ordersByStatus", byStatus);
        } catch (Exception e) {
            report.put("error", e.getMessage());
        }

        return report;
    }

    public Map<String, Object> getInventoryReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("reportType", "Inventory Report");
        report.put("generatedAt", LocalDateTime.now());
        return report;
    }

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            orderDbUrl, orderDbUsername, orderDbPassword);
    }
}
