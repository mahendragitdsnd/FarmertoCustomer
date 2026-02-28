package com.farmtohome.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.farmtohome.dto.AdminDashboardDTO;
import com.farmtohome.dto.AdminDashboardDTO.RecentOrderDTO;
import com.farmtohome.dto.AdminDashboardDTO.TopFarmerDTO;
import com.farmtohome.dto.AdminDashboardDTO.TopProductDTO;
import com.farmtohome.model.Order;
import com.farmtohome.model.OrderItem;
import com.farmtohome.model.Product;
import com.farmtohome.model.Role;
import com.farmtohome.model.User;
import com.farmtohome.repository.OrderItemRepository;
import com.farmtohome.repository.OrderRepository;
import com.farmtohome.repository.ProductRepository;
import com.farmtohome.repository.ReviewRepository;
import com.farmtohome.repository.UserRepository;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    public AdminDashboardDTO getDashboardStats() {
        AdminDashboardDTO dashboard = new AdminDashboardDTO();

        // User Statistics
        List<User> allUsers = userRepository.findAll();
        long totalFarmers = allUsers.stream().filter(u -> u.getRole() == Role.FARMER).count();
        long totalCustomers = allUsers.stream().filter(u -> u.getRole() == Role.CUSTOMER).count();
        long totalUsers = allUsers.size();

        dashboard.setTotalUsers(totalUsers);
        dashboard.setTotalFarmers(totalFarmers);
        dashboard.setTotalCustomers(totalCustomers);

        // Order Statistics
        List<Order> allOrders = orderRepository.findAll();
        long totalOrders = allOrders.size();
        long pendingOrders = allOrders.stream()
                .filter(o -> !o.getStatus().equals("DELIVERED") && 
                           !o.getStatus().equals("CANCELLED") && 
                           !o.getStatus().equals("REJECTED"))
                .count();
        long completedOrders = allOrders.stream()
                .filter(o -> o.getStatus().equals("DELIVERED"))
                .count();

        dashboard.setTotalOrders(totalOrders);
        dashboard.setPendingOrders(pendingOrders);
        dashboard.setCompletedOrders(completedOrders);

        // Revenue Statistics
        BigDecimal totalRevenue = allOrders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Double averageOrderValue = totalOrders > 0 ? 
                totalRevenue.doubleValue() / totalOrders : 0.0;

        dashboard.setTotalRevenue(totalRevenue);
        dashboard.setAverageOrderValue(averageOrderValue);

        // Product Statistics
        long totalProducts = productRepository.count();
        dashboard.setTotalProducts(totalProducts);

        // Top Products
        List<TopProductDTO> topProducts = getTopProducts();
        dashboard.setTopProducts(topProducts);

        // Top Farmers
        List<TopFarmerDTO> topFarmers = getTopFarmers();
        dashboard.setTopFarmers(topFarmers);

        // Recent Orders
        List<RecentOrderDTO> recentOrders = getRecentOrders();
        dashboard.setRecentOrders(recentOrders);

        return dashboard;
    }

    private List<TopProductDTO> getTopProducts() {
        List<OrderItem> allOrderItems = orderItemRepository.findAll();
        
        return allOrderItems.stream()
                .collect(Collectors.groupingBy(oi -> oi.getProduct(), 
                        Collectors.counting()))
                .entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(entry -> new TopProductDTO(
                        entry.getKey().getId(),
                        entry.getKey().getName(),
                        entry.getValue()
                ))
                .collect(Collectors.toList());
    }

    private List<TopFarmerDTO> getTopFarmers() {
        List<User> farmers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.FARMER)
                .sorted((a, b) -> Double.compare(b.getAverageRating(), a.getAverageRating()))
                .limit(5)
                .collect(Collectors.toList());

        return farmers.stream()
                .map(farmer -> {
                    List<OrderItem> farmerOrders = orderItemRepository.findByFarmerId(farmer.getId());
                    return new TopFarmerDTO(
                            farmer.getId(),
                            farmer.getName(),
                            farmer.getAverageRating(),
                            (long) farmerOrders.size()
                    );
                })
                .collect(Collectors.toList());
    }

    private List<RecentOrderDTO> getRecentOrders() {
        List<Order> allOrders = orderRepository.findAll();
        
        return allOrders.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(10)
                .map(order -> new RecentOrderDTO(
                        order.getId(),
                        order.getCustomer().getName(),
                        order.getStatus(),
                        order.getTotalAmount(),
                        order.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<User> allUsers = userRepository.findAll();
        long farmers = allUsers.stream().filter(u -> u.getRole() == Role.FARMER).count();
        long customers = allUsers.stream().filter(u -> u.getRole() == Role.CUSTOMER).count();
        
        stats.put("farmers", farmers);
        stats.put("customers", customers);
        stats.put("total", allUsers.size());
        
        return stats;
    }

    public Map<String, Object> getOrderStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<Order> allOrders = orderRepository.findAll();
        
        stats.put("total", allOrders.size());
        stats.put("pending", allOrders.stream()
                .filter(o -> !o.getStatus().equals("DELIVERED") && 
                           !o.getStatus().equals("CANCELLED") && 
                           !o.getStatus().equals("REJECTED"))
                .count());
        stats.put("completed", allOrders.stream()
                .filter(o -> o.getStatus().equals("DELIVERED"))
                .count());
        stats.put("cancelled", allOrders.stream()
                .filter(o -> o.getStatus().equals("CANCELLED"))
                .count());
        
        return stats;
    }

    public Map<String, Object> getProductStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<Product> allProducts = productRepository.findAll();
        
        stats.put("total", allProducts.size());
        stats.put("active", allProducts.stream()
                .filter(p -> p.getStatus().equals("ACTIVE"))
                .count());
        stats.put("paused", allProducts.stream()
                .filter(p -> p.getStatus().equals("PAUSED"))
                .count());
        stats.put("outOfStock", allProducts.stream()
                .filter(p -> p.getStatus().equals("OUT_OF_STOCK"))
                .count());
        
        return stats;
    }

    public List<Map<String, Object>> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", user.getId());
                    map.put("name", user.getName());
                    map.put("email", user.getEmail());
                    map.put("role", user.getRole());
                    map.put("rating", user.getAverageRating());
                    return map;
                })
                .collect(Collectors.toList());
    }
}
