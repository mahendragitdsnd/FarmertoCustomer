package com.farmtohome.controller;

import com.farmtohome.model.FarmerProfile;
import com.farmtohome.repository.FarmerProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/farmer")
public class FarmerController {

    @Autowired
    private FarmerProfileRepository farmerProfileRepository;

    @Autowired
    private com.farmtohome.service.OrderService orderService;

    @Autowired
    private com.farmtohome.service.ProductService productService;

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        FarmerProfile profile = farmerProfileRepository.findByUserId(userId).orElse(null);
        if (profile != null) {
            return ResponseEntity.ok(profile);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/analytics/{userId}")
    public ResponseEntity<?> getAnalytics(@PathVariable Long userId) {
        // Calculate Analytics
        java.util.List<com.farmtohome.model.Order> orders = orderService.getOrdersForFarmer(userId);

        java.math.BigDecimal totalSales = java.math.BigDecimal.ZERO;
        int activeOrders = 0;

        for (com.farmtohome.model.Order order : orders) {
            if ("DELIVERED".equals(order.getStatus())) {
                totalSales = totalSales.add(order.getTotalAmount());
            } else if (!"CANCELLED".equals(order.getStatus())) {
                activeOrders++;
            }
        }

        FarmerProfile profile = farmerProfileRepository.findByUserId(userId).orElse(null);
        com.farmtohome.model.User user = profile != null ? profile.getUser() : null;
        double rating = user != null ? user.getAverageRating() : 0.0;
        int reviews = user != null ? user.getRatingCount() : 0;

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("totalSales", totalSales);
        response.put("activeOrders", activeOrders);
        response.put("rating", rating);
        response.put("totalReviews", reviews);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody FarmerProfile profile) {
        return ResponseEntity.ok(farmerProfileRepository.save(profile));
    }
}
