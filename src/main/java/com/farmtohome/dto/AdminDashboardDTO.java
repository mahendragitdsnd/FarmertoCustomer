package com.farmtohome.dto;

import java.math.BigDecimal;
import java.util.List;

public class AdminDashboardDTO {
    private Long totalUsers;
    private Long totalFarmers;
    private Long totalCustomers;
    private Long totalOrders;
    private Long totalProducts;
    private BigDecimal totalRevenue;
    private Double averageOrderValue;
    private Long pendingOrders;
    private Long completedOrders;
    private List<TopProductDTO> topProducts;
    private List<TopFarmerDTO> topFarmers;
    private List<RecentOrderDTO> recentOrders;

    // Constructors
    public AdminDashboardDTO() {}

    public AdminDashboardDTO(Long totalUsers, Long totalFarmers, Long totalCustomers, 
                            Long totalOrders, Long totalProducts, BigDecimal totalRevenue,
                            Double averageOrderValue, Long pendingOrders, Long completedOrders) {
        this.totalUsers = totalUsers;
        this.totalFarmers = totalFarmers;
        this.totalCustomers = totalCustomers;
        this.totalOrders = totalOrders;
        this.totalProducts = totalProducts;
        this.totalRevenue = totalRevenue;
        this.averageOrderValue = averageOrderValue;
        this.pendingOrders = pendingOrders;
        this.completedOrders = completedOrders;
    }

    // Getters and Setters
    public Long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }

    public Long getTotalFarmers() { return totalFarmers; }
    public void setTotalFarmers(Long totalFarmers) { this.totalFarmers = totalFarmers; }

    public Long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(Long totalCustomers) { this.totalCustomers = totalCustomers; }

    public Long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }

    public Long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(Long totalProducts) { this.totalProducts = totalProducts; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public Double getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(Double averageOrderValue) { this.averageOrderValue = averageOrderValue; }

    public Long getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(Long pendingOrders) { this.pendingOrders = pendingOrders; }

    public Long getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(Long completedOrders) { this.completedOrders = completedOrders; }

    public List<TopProductDTO> getTopProducts() { return topProducts; }
    public void setTopProducts(List<TopProductDTO> topProducts) { this.topProducts = topProducts; }

    public List<TopFarmerDTO> getTopFarmers() { return topFarmers; }
    public void setTopFarmers(List<TopFarmerDTO> topFarmers) { this.topFarmers = topFarmers; }

    public List<RecentOrderDTO> getRecentOrders() { return recentOrders; }
    public void setRecentOrders(List<RecentOrderDTO> recentOrders) { this.recentOrders = recentOrders; }

    // Inner DTOs
    public static class TopProductDTO {
        private Long productId;
        private String productName;
        private Long orderCount;

        public TopProductDTO(Long productId, String productName, Long orderCount) {
            this.productId = productId;
            this.productName = productName;
            this.orderCount = orderCount;
        }

        public Long getProductId() { return productId; }
        public String getProductName() { return productName; }
        public Long getOrderCount() { return orderCount; }
    }

    public static class TopFarmerDTO {
        private Long farmerId;
        private String farmerName;
        private Double rating;
        private Long orderCount;

        public TopFarmerDTO(Long farmerId, String farmerName, Double rating, Long orderCount) {
            this.farmerId = farmerId;
            this.farmerName = farmerName;
            this.rating = rating;
            this.orderCount = orderCount;
        }

        public Long getFarmerId() { return farmerId; }
        public String getFarmerName() { return farmerName; }
        public Double getRating() { return rating; }
        public Long getOrderCount() { return orderCount; }
    }

    public static class RecentOrderDTO {
        private Long orderId;
        private String customerName;
        private String status;
        private java.math.BigDecimal totalAmount;
        private java.time.LocalDateTime createdAt;

        public RecentOrderDTO(Long orderId, String customerName, String status, 
                             java.math.BigDecimal totalAmount, java.time.LocalDateTime createdAt) {
            this.orderId = orderId;
            this.customerName = customerName;
            this.status = status;
            this.totalAmount = totalAmount;
            this.createdAt = createdAt;
        }

        public Long getOrderId() { return orderId; }
        public String getCustomerName() { return customerName; }
        public String getStatus() { return status; }
        public java.math.BigDecimal getTotalAmount() { return totalAmount; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    }
}
