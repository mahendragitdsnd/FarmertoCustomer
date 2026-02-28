package com.farmtohome.model;

public enum OrderStatus {
    PLACED,
    WAITING_FOR_FARMER_APPROVAL,
    FARMER_APPROVED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED,
    REJECTED
}
