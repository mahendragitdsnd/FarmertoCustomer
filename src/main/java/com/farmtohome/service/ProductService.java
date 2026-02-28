package com.farmtohome.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.farmtohome.model.Product;
import com.farmtohome.model.User;
import com.farmtohome.repository.ProductRepository;
import com.farmtohome.repository.UserRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public Product addProduct(Product product, Long farmerId) {
        User farmer = userRepository.findById(farmerId)
                .orElseThrow(() -> new RuntimeException("Farmer not found"));
        product.setFarmer(farmer);
        product.setStatus("ACTIVE");
        return productRepository.save(product);
    }

    public Product updateProduct(Long productId, Product productInterval, Long farmerId) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!existingProduct.getFarmer().getId().equals(farmerId)) {
            throw new RuntimeException("Unauthorized");
        }

        existingProduct.setName(productInterval.getName());
        existingProduct.setCategory(productInterval.getCategory());
        existingProduct.setPrice(productInterval.getPrice());
        existingProduct.setQuantity(productInterval.getQuantity());
        existingProduct.setDescription(productInterval.getDescription());

        // Only update image if a new one is provided
        if (productInterval.getImageUrl() != null && !productInterval.getImageUrl().isEmpty()) {
            existingProduct.setImageUrl(productInterval.getImageUrl());
        }

        return productRepository.save(existingProduct);
    }

    public void deleteProduct(Long productId, Long farmerId) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!existingProduct.getFarmer().getId().equals(farmerId)) {
            throw new RuntimeException("Unauthorized");
        }

        // Mark product as OUT_OF_STOCK instead of deleting
        // This allows customers to see the product in their cart with out-of-stock status
        existingProduct.setStatus("OUT_OF_STOCK");
        productRepository.save(existingProduct);
    }

    public List<Product> getProductsByFarmer(Long farmerId) {
        return productRepository.findByFarmerIdAndStatusIn(farmerId, java.util.Arrays.asList("ACTIVE", "PAUSED"));
    }

    public List<Product> getAllActiveProducts() {
        return productRepository.findByStatusIn(java.util.Arrays.asList("ACTIVE", "PAUSED"));
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public List<Product> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCaseAndStatusIn(query,
                java.util.Arrays.asList("ACTIVE", "PAUSED"));
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryAndStatusIn(category, java.util.Arrays.asList("ACTIVE", "PAUSED"));
    }

    public Product toggleStatus(Long productId, Long farmerId) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (!existingProduct.getFarmer().getId().equals(farmerId)) {
            throw new RuntimeException("Unauthorized");
        }

        if ("ACTIVE".equals(existingProduct.getStatus())) {
            existingProduct.setStatus("PAUSED");
        } else if ("PAUSED".equals(existingProduct.getStatus())) {
            existingProduct.setStatus("ACTIVE");
        }
        // If INACTIVE (Deleted), do nothing or throw error
        return productRepository.save(existingProduct);
    }
}
