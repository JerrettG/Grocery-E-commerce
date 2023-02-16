package com.gonsalves.orderservice.util;

import com.gonsalves.orderservice.repository.entity.AddressInfoEntity;
import com.gonsalves.orderservice.repository.entity.OrderEntity;
import com.gonsalves.orderservice.repository.entity.OrderItemEntity;
import com.gonsalves.orderservice.repository.entity.Status;
import com.gonsalves.orderservice.service.model.AddressInfo;
import com.gonsalves.orderservice.service.model.Order;
import com.gonsalves.orderservice.service.model.OrderItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TypeConverter {
    private static final Gson gson = new GsonBuilder().create();


    public static <T> String toJson(T object) {
        return gson.toJson(object);
    }
    public static List<Order> fromJsonToOrderList(String json) {
        return gson.fromJson(json, new TypeToken<ArrayList<Order>>() { }.getType());
    }

    public static Order fromJsonToOrder(String json) {
        return gson.fromJson(json, Order.class);
    }

    public static Order convertFromEntity(OrderEntity entity) {
        List<OrderItem> orderItems = entity.getOrderItemEntities().stream()
                .map(TypeConverter::convertFromEntity)
                .collect(Collectors.toList());

        return Order.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .paymentIntentId(entity.getPaymentIntentId())
                .shippingInfo(convertFromEntity(entity.getShippingInfo()))
                .billingInfo(convertFromEntity(entity.getBillingInfo()))
                .subtotal(entity.getSubtotal())
                .tax(entity.getTax())
                .shippingCost(entity.getShippingCost())
                .total(entity.getTotal())
                .status(entity.getStatus().toString())
                .orderItems(orderItems)
                .createdDate(entity.getCreatedDate())
                .build();


    }
    public static OrderItem convertFromEntity(OrderItemEntity entity) {
        return new OrderItem(
                entity.getItemName(),
                entity.getImageUrl(),
                entity.getQuantity(),
                entity.getUnitPrice()
        );
    }
    public static AddressInfo convertFromEntity(AddressInfoEntity entity) {
        return new AddressInfo(
                entity.getFirstName(),
                entity.getLastName(),
                entity.getAddressFirstLine(),
                entity.getAddressSecondLine(),
                entity.getCity(),
                entity.getState(),
                entity.getZipCode()
        );
    }
    public static OrderEntity convertToEntity(Order order) {
        List<OrderItemEntity> orderItemEntities = order.getOrderItems().stream()
                .map(TypeConverter::convertToEntity)
                .collect(Collectors.toList());

        return OrderEntity.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .paymentIntentId(order.getPaymentIntentId())
                .shippingInfo(convertToEntity(order.getShippingInfo()))
                .billingInfo(convertToEntity(order.getBillingInfo()))
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .shippingCost(order.getShippingCost())
                .total(order.getTotal())
                .status(Status.valueOf(order.getStatus()))
                .orderItemEntities(orderItemEntities)
                .createdDate(order.getCreatedDate())
                .build();
    }
    public static OrderItemEntity convertToEntity(OrderItem orderItem) {
        return new OrderItemEntity(
                orderItem.getItemName(),
                orderItem.getImageUrl(),
                orderItem.getQuantity(),
                orderItem.getUnitPrice()
        );
    }
    public static AddressInfoEntity convertToEntity(AddressInfo addressInfo) {
        return new AddressInfoEntity(
                addressInfo.getFirstName(),
                addressInfo.getLastName(),
                addressInfo.getAddressFirstLine(),
                addressInfo.getAddressSecondLine(),
                addressInfo.getCity(),
                addressInfo.getState(),
                addressInfo.getZipCode()
        );
    }
    
}
