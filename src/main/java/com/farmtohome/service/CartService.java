package com.farmtohome.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.farmtohome.model.Cart;
import com.farmtohome.model.CartItem;
import com.farmtohome.model.Product;
import com.farmtohome.model.User;
import com.farmtohome.repository.CartItemRepository;
import com.farmtohome.repository.CartRepository;
import com.farmtohome.repository.ProductRepository;
import com.farmtohome.repository.UserRepository;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public Cart getCart(Long customerId) {
        return cartRepository.findByCustomerId(customerId).orElseGet(() -> {
            User customer = userRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            Cart newCart = new Cart();
            newCart.setCustomer(customer);
            return cartRepository.save(newCart);
        });
    }

    public void updateCartItemQuantity(Long cartItemId, Integer quantity) {
        if (quantity <= 0) {
            cartItemRepository.deleteById(cartItemId);
        } else {
            CartItem item = cartItemRepository.findById(cartItemId)
                    .orElseThrow(() -> new RuntimeException("Item not found"));
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
    }

    public void addToCart(Long customerId, Long productId, Integer quantity) {
        Cart cart = getCart(customerId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Prevent adding out-of-stock or inactive products
        if ("OUT_OF_STOCK".equals(product.getStatus())) {
            throw new RuntimeException("Product is out of stock: " + product.getName());
        }
        if ("INACTIVE".equals(product.getStatus())) {
            throw new RuntimeException("Product is no longer available: " + product.getName());
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cartItemRepository.save(newItem);
        }
    }

    public void removeFromCart(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    public java.util.List<CartItem> getCartItems(Long customerId) {
        Cart cart = getCart(customerId);
        return cartItemRepository.findByCartId(cart.getId());
    }
}
