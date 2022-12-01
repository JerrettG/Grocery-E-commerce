package com.gonsalves.ui.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
@Builder
@Data
@NoArgsConstructor
public class Order {


    private String id;
    private String userId;
    private String paymentIntentId;
    private String shippingAddress;
    private double orderTotal;
    private String status;
    private List<OrderItem> orderItems;
    private String createdDate;

    public Order(String id, String userId, String paymentIntentId, String shippingAddress, double orderTotal, String status,
                 List<OrderItem> orderItems, String createdDate) {
        this.id = id;
        this.userId = userId;
        this.paymentIntentId = paymentIntentId;
        this.shippingAddress = shippingAddress;
        this.orderTotal = orderTotal;
        this.status = status;
        this.orderItems = orderItems;
        this.createdDate = formatLocalDateTime(createdDate);
    }
    private String formatLocalDateTime(String localDateTime) {
        if (localDateTime == null)
            return null;
        ZonedDateTime ldt = ZonedDateTime.parse(localDateTime);
        return DateTimeFormatter.ofPattern("MMMM dd, yyyy").format(ldt);
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = formatLocalDateTime(createdDate);
    }

    public void calculateAndSetOrderTotal() {
        double total = 0;
        for (OrderItem orderItem: orderItems) {
            total += orderItem.getQuantity()*orderItem.getUnitPrice();
        }
        orderTotal = total;
    }

}