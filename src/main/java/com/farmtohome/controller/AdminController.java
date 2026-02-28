package com.farmtohome.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.farmtohome.dto.AdminDashboardDTO;
import com.farmtohome.service.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // Admin: Get Dashboard Stats
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDTO> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // Admin: Get User Statistics
    @GetMapping("/stats/users")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        return ResponseEntity.ok(adminService.getUserStatistics());
    }

    // Admin: Get Order Statistics
    @GetMapping("/stats/orders")
    public ResponseEntity<Map<String, Object>> getOrderStats() {
        return ResponseEntity.ok(adminService.getOrderStatistics());
    }

    // Admin: Get Product Statistics
    @GetMapping("/stats/products")
    public ResponseEntity<Map<String, Object>> getProductStats() {
        return ResponseEntity.ok(adminService.getProductStatistics());
    }

    // Admin: Get All Users
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }
}
