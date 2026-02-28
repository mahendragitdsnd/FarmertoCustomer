package com.farmtohome.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.farmtohome.model.Address;
import com.farmtohome.model.Cart;
import com.farmtohome.model.CartItem;
import com.farmtohome.model.Order;
import com.farmtohome.model.OrderItem;
import com.farmtohome.model.OrderStatus;
import com.farmtohome.model.Product;
import com.farmtohome.model.User;
import com.farmtohome.repository.AddressRepository;
import com.farmtohome.repository.CartItemRepository;
import com.farmtohome.repository.CartRepository;
import com.farmtohome.repository.OrderItemRepository;
import com.farmtohome.repository.OrderRepository;
import com.farmtohome.repository.ProductRepository;
import com.farmtohome.repository.UserRepository;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ProductRepository productRepository;

    // Farmer: Get orders involving this farmer
    // Since we decided to split orders by farmer on checkout, we can just find
    // Orders where one of the items belongs to the farmer?
    // Actually, if we split orders by farmer, we can add 'farmer_id' to the Order
    // table to make looking up easier?
    // Or we can rely on `OrderItem` having farmer_id and join.
    // However, if we split orders, each Order will only have items from ONE farmer.
    // So distinct Order IDs from OrderItems where farmer_id = X.

    public List<Order> getOrdersForFarmer(Long farmerId) {
        List<OrderItem> items = orderItemRepository.findByFarmerId(farmerId);
        return items.stream().map(OrderItem::getOrder).distinct().collect(Collectors.toList());
    }

    public Order updateOrderStatus(Long orderId, String status, Long farmerId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));

        // Verify this order belongs to the farmer (check first item)
        OrderItem firstItem = orderItemRepository.findByFarmerId(farmerId).stream()
                .filter(item -> item.getOrder().getId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Access Denied: This order does not belong to you"));

        if ("DELIVERED".equals(status) && !"DELIVERED".equals(order.getStatus())) {
            // Reduce Stock
            List<OrderItem> items = orderItemRepository.findByFarmerId(farmerId).stream()
                    .filter(item -> item.getOrder().getId().equals(orderId))
                    .collect(Collectors.toList());

            for (OrderItem item : items) {
                Product p = item.getProduct();
                p.setQuantity(p.getQuantity() - item.getQuantity());
                if (p.getQuantity() < 0)
                    p.setQuantity(0); // Safety
                productRepository.save(p);
            }
        }

        order.setStatus(status);
        return orderRepository.save(order);
    }

    public List<Order> getCustomerOrders(Long customerId) {
        return orderRepository.findByCustomerIdWithItems(customerId);
    }

    @Transactional
    public List<Order> placeOrder(com.farmtohome.dto.OrderRequest request) {
        Long customerId = request.getCustomerId();
        Long addressId = request.getAddressId();
        String paymentMethod = request.getPaymentMethod();

        // Validate
        if (customerId == null || addressId == null)
            throw new RuntimeException("Invalid Customer or Address");

        Cart cart = cartRepository.findByCustomerId(customerId).orElseThrow(() -> new RuntimeException("Cart empty"));
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Group items by Farmer
        Map<User, List<CartItem>> itemsByFarmer = cartItems.stream()
                .collect(Collectors.groupingBy(item -> item.getProduct().getFarmer()));

        List<Order> createdOrders = new ArrayList<>();

        for (Map.Entry<User, List<CartItem>> entry : itemsByFarmer.entrySet()) {
            User farmer = entry.getKey();
            List<CartItem> farmerItems = entry.getValue();

            Order order = new Order();
            order.setCustomer(customer);
            order.setAddress(address);
            if(request.getContactNumber() != null) {
                order.setContactNumber(request.getContactNumber());
            }

            // Logic: If mock payment success (UPI/CARD), we can set to
            // WAITING_FOR_FARMER_APPROVAL.
            // Requirement said: Mock payment -> SUCCESS.
            order.setStatus(OrderStatus.WAITING_FOR_FARMER_APPROVAL.name());
            order.setCreatedAt(LocalDateTime.now());

            // TODO: Store Payment Details (For now, we just proceed as if paid)

            BigDecimal infoTotal = BigDecimal.ZERO;

            Order savedOrder = orderRepository.save(order);

            for (CartItem ci : farmerItems) {
                // Check product status - reject if OUT_OF_STOCK or INACTIVE
                Product product = ci.getProduct();
                if ("OUT_OF_STOCK".equals(product.getStatus())) {
                    throw new RuntimeException("Product out of stock: " + product.getName() + ". Please remove it from your cart and try again.");
                }
                if ("INACTIVE".equals(product.getStatus())) {
                    throw new RuntimeException("Product no longer available: " + product.getName() + ". Please remove it from your cart and try again.");
                }

                OrderItem oi = new OrderItem();
                oi.setOrder(savedOrder);
                oi.setProduct(product);
                oi.setFarmer(farmer);
                oi.setQuantity(ci.getQuantity());
                oi.setPrice(product.getPrice());

                // Check Availability
                Integer pendingQty = orderItemRepository.sumPendingQuantities(product.getId());
                int available = product.getQuantity() - pendingQty;
                if (available < ci.getQuantity()) {
                    throw new RuntimeException("Insufficient stock for product: " + product.getName());
                }

                orderItemRepository.save(oi);

                BigDecimal lineTotal = ci.getProduct().getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
                infoTotal = infoTotal.add(lineTotal);
            }

            savedOrder.setTotalAmount(infoTotal);
            createdOrders.add(orderRepository.save(savedOrder));
        }

        // Clear cart
        cartItemRepository.deleteByCartId(cart.getId());

        return createdOrders;
    }

    @Transactional
    public Order cancelOrder(Long orderId, Long customerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new RuntimeException("Unauthorized: This order does not belong to you");
        }

        // Validate Status
        // Allowed: PLACED, WAITING_FOR_FARMER_APPROVAL, FARMER_APPROVED
        // Denied: OUT_FOR_DELIVERY, DELIVERED, CANCELLED, REJECTED
        String s = order.getStatus();
        if ("OUT_FOR_DELIVERY".equals(s) || "DELIVERED".equals(s) || "CANCELLED".equals(s) || "REJECTED".equals(s)) {
            throw new RuntimeException("Order cannot be cancelled in current status: " + s);
        }

        order.setStatus("CANCELLED");
        // TODO: Log refund if payment was made?

        return orderRepository.save(order);
    }
}
