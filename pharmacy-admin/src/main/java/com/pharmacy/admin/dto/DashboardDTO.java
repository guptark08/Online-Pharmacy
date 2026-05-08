package com.pharmacy.admin.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardDTO {

    private long totalOrders;
    private long pendingPrescriptions;
    private long lowStockMedicines;
    private long totalMedicines;
    private long totalCategories;
    private BigDecimal totalRevenue;
    private List<OrderSummaryDTO> recentOrders;

    public DashboardDTO() {}

    public long getTotalOrders() { return totalOrders; }
    public long getPendingPrescriptions() { return pendingPrescriptions; }
    public long getLowStockMedicines() { return lowStockMedicines; }
    public long getTotalMedicines() { return totalMedicines; }
    public long getTotalCategories() { return totalCategories; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public List<OrderSummaryDTO> getRecentOrders() { return recentOrders; }

    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
    public void setPendingPrescriptions(long pendingPrescriptions) { this.pendingPrescriptions = pendingPrescriptions; }
    public void setLowStockMedicines(long lowStockMedicines) { this.lowStockMedicines = lowStockMedicines; }
    public void setTotalMedicines(long totalMedicines) { this.totalMedicines = totalMedicines; }
    public void setTotalCategories(long totalCategories) { this.totalCategories = totalCategories; }
    public void setTotalRevenue(BigDecimal bigDecimal) { this.totalRevenue = bigDecimal; }
    public void setRecentOrders(List<OrderSummaryDTO> recentOrders) { this.recentOrders = recentOrders; }
}
