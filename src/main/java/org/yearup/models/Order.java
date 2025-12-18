package org.yearup.models;

import java.time.LocalDateTime;

public class Order {
    private int orderId;
    private int userId;
    private LocalDateTime orderDate;
    private String status;

    public Order() {
    }

    public Order(int userId) {
        this.userId = userId;
        this.orderDate = LocalDateTime.now();
        this.status = "NEW";
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}