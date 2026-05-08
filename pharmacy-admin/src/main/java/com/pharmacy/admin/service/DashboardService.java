package com.pharmacy.admin.service;

import com.pharmacy.admin.dto.DashboardDTO;
import com.pharmacy.admin.dto.OrderSummaryDTO;
import com.pharmacy.admin.enums.OrderStatus;
import com.pharmacy.admin.enums.PrescriptionStatus;
import com.pharmacy.admin.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    @Autowired
    private MedicineRepository medicineRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Value("${order.datasource.url}")
    private String orderDbUrl;

    @Value("${order.datasource.username}")
    private String orderDbUsername;

    @Value("${order.datasource.password}")
    private String orderDbPassword;

    public DashboardDTO getDashboard() {
        DashboardDTO dto = new DashboardDTO();

        // From Catalog DB
        dto.setTotalMedicines(
            medicineRepository.findByIsActiveTrue().size());
        dto.setTotalCategories(
            categoryRepository.findByIsActiveTrue().size());
        dto.setPendingPrescriptions(
            prescriptionRepository.findByStatus(PrescriptionStatus.PENDING).size());
        dto.setLowStockMedicines(
            medicineRepository.findByStockLessThanAndIsActiveTrue(10).size());

        // From Order DB using JDBC
        try (Connection conn = getConnection();
             PreparedStatement ps1 = conn.prepareStatement(
                 "SELECT COUNT(*) FROM orders");
             PreparedStatement ps2 = conn.prepareStatement(
                 "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE status = 'PAID'");
             PreparedStatement ps3 = conn.prepareStatement(
                 "SELECT id, user_email, status, total_amount, created_at " +
                 "FROM orders ORDER BY created_at DESC LIMIT 5")) {
            // Total orders
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) dto.setTotalOrders(rs1.getLong(1));

            // Total revenue
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) dto.setTotalRevenue(
                rs2.getBigDecimal(1));

            // Recent orders
            ResultSet rs3 = ps3.executeQuery();
            List<OrderSummaryDTO> recentOrders = new ArrayList<>();
            while (rs3.next()) {
                OrderSummaryDTO o = new OrderSummaryDTO();
                o.setId(rs3.getLong("id"));
                o.setUserEmail(rs3.getString("user_email"));
                o.setStatus(OrderStatus.valueOf(rs3.getString("status")));
                o.setTotalAmount(rs3.getBigDecimal("total_amount"));
                o.setCreatedAt(rs3.getTimestamp("created_at")
                    .toLocalDateTime());
                recentOrders.add(o);
            }
            dto.setRecentOrders(recentOrders);
        } catch (Exception e) {
            dto.setTotalOrders(0);
            dto.setTotalRevenue(BigDecimal.ZERO);
            dto.setRecentOrders(new ArrayList<>());
        }

        return dto;
    }

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            orderDbUrl, orderDbUsername, orderDbPassword);
    }
}
