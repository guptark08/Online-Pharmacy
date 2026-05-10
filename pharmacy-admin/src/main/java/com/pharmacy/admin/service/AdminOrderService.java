package com.pharmacy.admin.service;

import com.pharmacy.admin.dto.OrderStatusRequest;
import com.pharmacy.admin.dto.OrderSummaryDTO;
import com.pharmacy.admin.enums.OrderStatus;
import com.pharmacy.admin.entity.Prescription;
import com.pharmacy.admin.enums.PrescriptionStatus;
import com.pharmacy.admin.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AdminOrderService {

    private final PrescriptionRepository prescriptionRepository;

    public AdminOrderService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    @Value("${order.datasource.url}")
    private String orderDbUrl;

    @Value("${order.datasource.username}")
    private String orderDbUsername;

    @Value("${order.datasource.password}")
    private String orderDbPassword;

    public List<OrderSummaryDTO> getAllOrders() {
        List<OrderSummaryDTO> orders = new ArrayList<>();
        String sql = "SELECT id, user_email, status, total_amount, prescription_id, " +
                     "created_at FROM orders ORDER BY created_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                OrderSummaryDTO o = new OrderSummaryDTO();
                o.setId(rs.getLong("id"));
                o.setUserEmail(rs.getString("user_email"));
                o.setStatus(OrderStatus.valueOf(rs.getString("status")));
                o.setTotalAmount(rs.getBigDecimal("total_amount"));
                o.setCreatedAt(rs.getTimestamp("created_at")
                    .toLocalDateTime());
                Long prescriptionId = (Long) rs.getObject("prescription_id");
                o.setPrescriptionId(prescriptionId);
                enrichPrescriptionDetails(o);
                orders.add(o);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching orders: "
                + e.getMessage());
        }
        return orders;
    }

    public OrderSummaryDTO updateOrderStatus(Long orderId,
                                              OrderStatusRequest request) {
        String updateSql = "UPDATE orders SET status = ? WHERE id = ?";
        String selectSql = "SELECT id, user_email, status, prescription_id, " +
                           "total_amount, created_at FROM orders WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement updatePs = conn.prepareStatement(updateSql);
             PreparedStatement selectPs = conn.prepareStatement(selectSql)) {
            // Update status
            updatePs.setString(1, request.getStatus());
            updatePs.setLong(2, orderId);
            updatePs.executeUpdate();
            syncPrescriptionReview(orderId, OrderStatus.valueOf(request.getStatus()));

            // Fetch updated order
            selectPs.setLong(1, orderId);
            ResultSet rs = selectPs.executeQuery();

            if (rs.next()) {
                OrderSummaryDTO o = new OrderSummaryDTO();
                o.setId(rs.getLong("id"));
                o.setUserEmail(rs.getString("user_email"));
                o.setStatus(OrderStatus.valueOf(rs.getString("status")));
                o.setTotalAmount(rs.getBigDecimal("total_amount"));
                o.setCreatedAt(rs.getTimestamp("created_at")
                    .toLocalDateTime());
                Long prescriptionId = (Long) rs.getObject("prescription_id");
                o.setPrescriptionId(prescriptionId);
                enrichPrescriptionDetails(o);
                return o;
            }
            throw new RuntimeException("Order not found: " + orderId);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error updating order: "
                + e.getMessage());
        }
    }

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            orderDbUrl, orderDbUsername, orderDbPassword);
    }

    private void enrichPrescriptionDetails(OrderSummaryDTO order) {
        Optional<Prescription> prescription = Optional.empty();

        if (order.getPrescriptionId() != null) {
            prescription = Optional.ofNullable(
                prescriptionRepository.findById(order.getPrescriptionId()))
                .orElse(Optional.empty());
        }

        if (prescription.isEmpty()) {
            prescription = Optional.ofNullable(
                prescriptionRepository.findByOrderId(order.getId()))
                .orElse(Optional.empty());
            prescription.ifPresent(value -> order.setPrescriptionId(value.getId()));
        }

        prescription.ifPresent(value -> {
            order.setPrescriptionFileName(value.getFileName());
            order.setPrescriptionFileType(value.getFileType());
            order.setPrescriptionStatus(value.getStatus());
        });
    }

    private void syncPrescriptionReview(Long orderId, OrderStatus orderStatus) {
        if (orderStatus != OrderStatus.PRESCRIPTION_APPROVED &&
            orderStatus != OrderStatus.PRESCRIPTION_REJECTED) {
            return;
        }

        PrescriptionStatus prescriptionStatus =
            orderStatus == OrderStatus.PRESCRIPTION_APPROVED ?
                PrescriptionStatus.APPROVED :
                PrescriptionStatus.REJECTED;

        prescriptionRepository.findByOrderId(orderId).ifPresent(prescription -> {
            prescription.setStatus(prescriptionStatus);
            prescription.setReviewedAt(LocalDateTime.now());
            prescription.setReviewNote(
                prescriptionStatus == PrescriptionStatus.APPROVED ?
                    "Approved from admin order review." :
                    "Rejected from admin order review.");
            prescriptionRepository.save(prescription);
        });
    }
}
