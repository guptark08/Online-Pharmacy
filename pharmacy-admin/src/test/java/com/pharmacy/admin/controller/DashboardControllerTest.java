package com.pharmacy.admin.controller;

import com.pharmacy.admin.dto.DashboardDTO;
import com.pharmacy.admin.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getDashboard_ShouldReturnDashboardData() throws Exception {
        // Given
        DashboardDTO dashboardDTO = new DashboardDTO();
        dashboardDTO.setTotalOrders(50L);
        dashboardDTO.setTotalMedicines(200L);
        dashboardDTO.setTotalCategories(12L);
        dashboardDTO.setTotalRevenue(BigDecimal.valueOf(5000.0));

        when(dashboardService.getDashboard()).thenReturn(dashboardDTO);

        // When & Then
        // GET request mein csrf() ki zaroorat nahi — sirf state-changing requests mein chahiye
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(50))
                .andExpect(jsonPath("$.totalMedicines").value(200))
                .andExpect(jsonPath("$.totalCategories").value(12))
                .andExpect(jsonPath("$.totalRevenue").value(5000.0));
    }
}
