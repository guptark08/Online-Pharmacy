package com.pharmacy.admin.service;

import com.pharmacy.admin.dto.DashboardDTO;
import com.pharmacy.admin.entity.Category;
import com.pharmacy.admin.entity.Medicine;
import com.pharmacy.admin.entity.Prescription;
import com.pharmacy.admin.repository.CategoryRepository;
import com.pharmacy.admin.repository.MedicineRepository;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static com.pharmacy.admin.enums.PrescriptionStatus.PENDING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement totalOrdersStatement;

    @Mock
    private PreparedStatement totalRevenueStatement;

    @Mock
    private PreparedStatement recentOrdersStatement;

    @Mock
    private ResultSet totalOrdersResult;

    @Mock
    private ResultSet totalRevenueResult;

    @Mock
    private ResultSet recentOrdersResult;

    @Spy
    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getDashboardCombinesRepositoryAndJdbcData() throws Exception {
        Medicine medicine = new Medicine();
        Category category = new Category();
        Timestamp createdAt = Timestamp.valueOf(LocalDateTime.now());

        when(medicineRepository.findByIsActiveTrue()).thenReturn(List.of(medicine, medicine));
        when(categoryRepository.findByIsActiveTrue()).thenReturn(List.of(category));
        when(prescriptionRepository.findByStatus(PENDING))
            .thenReturn(List.of(new Prescription(), new Prescription(), new Prescription()));
        when(medicineRepository.findByStockLessThanAndIsActiveTrue(10)).thenReturn(List.of(medicine));

        doReturn(connection).when(dashboardService).getConnection();
        when(connection.prepareStatement(anyString()))
            .thenReturn(totalOrdersStatement, totalRevenueStatement, recentOrdersStatement);
        when(totalOrdersStatement.executeQuery()).thenReturn(totalOrdersResult);
        when(totalRevenueStatement.executeQuery()).thenReturn(totalRevenueResult);
        when(recentOrdersStatement.executeQuery()).thenReturn(recentOrdersResult);
        when(totalOrdersResult.next()).thenReturn(true);
        when(totalOrdersResult.getLong(1)).thenReturn(7L);
        when(totalRevenueResult.next()).thenReturn(true);
        when(totalRevenueResult.getBigDecimal(1)).thenReturn(BigDecimal.valueOf(123.45));
        when(recentOrdersResult.next()).thenReturn(true, false);
        when(recentOrdersResult.getLong("id")).thenReturn(1L);
        when(recentOrdersResult.getString("user_email")).thenReturn("user@example.com");
        when(recentOrdersResult.getString("status")).thenReturn("PAID");
        when(recentOrdersResult.getBigDecimal("total_amount")).thenReturn(BigDecimal.valueOf(45));
        when(recentOrdersResult.getTimestamp("created_at")).thenReturn(createdAt);

        DashboardDTO dashboard = dashboardService.getDashboard();

        assertEquals(2, dashboard.getTotalMedicines());
        assertEquals(1, dashboard.getTotalCategories());
        assertEquals(3, dashboard.getPendingPrescriptions());
        assertEquals(1, dashboard.getLowStockMedicines());
        assertEquals(7, dashboard.getTotalOrders());
        assertEquals(BigDecimal.valueOf(123.45), dashboard.getTotalRevenue());
        assertEquals(1, dashboard.getRecentOrders().size());
    }

    @Test
    void getDashboardFallsBackWhenJdbcFails() throws Exception {
        when(medicineRepository.findByIsActiveTrue()).thenReturn(List.of());
        when(categoryRepository.findByIsActiveTrue()).thenReturn(List.of());
        when(prescriptionRepository.findByStatus(PENDING)).thenReturn(List.of());
        when(medicineRepository.findByStockLessThanAndIsActiveTrue(10)).thenReturn(List.of());
        doThrow(new RuntimeException("db unavailable")).when(dashboardService).getConnection();

        DashboardDTO dashboard = dashboardService.getDashboard();

        assertEquals(0, dashboard.getTotalOrders());
        assertEquals(BigDecimal.ZERO, dashboard.getTotalRevenue());
        assertTrue(dashboard.getRecentOrders().isEmpty());
    }
}
