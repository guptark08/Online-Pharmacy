package com.pharmacy.admin.controller;

import com.pharmacy.admin.service.ReportService;
import com.pharmacy.admin.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSalesReport_ShouldReturnSalesData() throws Exception {
        // Given
        Map<String, Object> salesReport = Map.of(
            "totalRevenue", 10000.0,
            "totalOrders", 100
        );

        when(reportService.getSalesReport()).thenReturn(salesReport);

        // When & Then
        mockMvc.perform(get("/api/admin/reports/sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRevenue").value(10000.0))
                .andExpect(jsonPath("$.totalOrders").value(100));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getInventoryReport_ShouldReturnInventoryData() throws Exception {
        // Given
        Map<String, Object> inventoryReport = Map.of(
            "totalMedicines", 500,
            "lowStockItems", 10
        );

        when(reportService.getInventoryReport()).thenReturn(inventoryReport);

        // When & Then
        mockMvc.perform(get("/api/admin/reports/inventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMedicines").value(500))
                .andExpect(jsonPath("$.lowStockItems").value(10));
    }
}
