package com.gonsalves.orderservice.service;

import com.gonsalves.orderservice.config.CacheStore;
import com.gonsalves.orderservice.exception.OrderAlreadyExistsException;
import com.gonsalves.orderservice.exception.OrderNotFoundException;
import com.gonsalves.orderservice.repository.OrderRepository;
import com.gonsalves.orderservice.repository.entity.AddressInfoEntity;
import com.gonsalves.orderservice.repository.entity.OrderEntity;
import com.gonsalves.orderservice.repository.entity.OrderItemEntity;
import com.gonsalves.orderservice.repository.entity.Status;
import com.gonsalves.orderservice.service.model.AddressInfo;
import com.gonsalves.orderservice.service.model.Order;
import com.gonsalves.orderservice.service.model.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {


    private final OrderRepository orderRepository;
    private final CacheStore cache;

    @Autowired
    public OrderService(OrderRepository orderRepository, CacheStore cache) {
        this.orderRepository = orderRepository;
        this.cache = cache;
    }

    public List<Order> getAllOrdersByUserId(String userId) {
        List<Order> cachedOrders = cache.get(userId);
        if (cachedOrders != null)
            return cachedOrders;

        List<Order> ordersFromDatabase = new ArrayList<>();
        orderRepository.getAllOrdersByUserId(userId)
                .forEach(entity -> ordersFromDatabase.add(convertFromEntity(entity)));
        cache.add(userId, ordersFromDatabase);

        return ordersFromDatabase;
    }


    public Order getOrderByOrderId(String userId, String orderId) {
        OrderEntity entity = orderRepository.getOrderByOrderId(userId, orderId);
        if (entity == null)
            throw new OrderNotFoundException("Order with order id could not be found.");
        return convertFromEntity(entity);
    }

    public Order getOrderByPaymentIntendId(String userId, String paymentIntentId) {
        List<OrderEntity> results = orderRepository.getOrderByPaymentIntentId(userId, paymentIntentId);
        if (results.size() == 0)
            throw new OrderNotFoundException("Order with payment id could not be found.");
        return convertFromEntity(results.get(0));
    }

    public Order createOrder(Order order) {
        List<OrderEntity> results = orderRepository.getOrderByPaymentIntentId(order.getUserId(), order.getPaymentIntentId());
        if (results.size() > 0)
            throw new OrderAlreadyExistsException("Cannot create order. Order already created for this transaction.");
        order.setId(UUID.randomUUID().toString());
        order.setCreatedDate(LocalDateTime.now().toString());
        orderRepository.createOrder(convertToEntity(order));
        cache.evict(order.getUserId());
        return order;
    }

    public void updateOrder(Order order) {
        getOrderByOrderId(order.getUserId(), order.getId());
        orderRepository.updateOrder(convertToEntity(order));
        cache.evict(order.getUserId());
    }

    public void cancelOrder(Order order) {
        OrderEntity entity = convertToEntity(order);
        entity.setStatus(Status.CANCELLED);
        orderRepository.updateOrder(entity);
        cache.evict(order.getUserId());
    }

    public void deleteOrder(Order order) {
        getOrderByOrderId(order.getUserId(), order.getId());
        orderRepository.deleteOrder(OrderEntity.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .build());
        cache.evict(order.getUserId());
    }
    private Order convertFromEntity(OrderEntity entity) {
        List<OrderItem> orderItems = new ArrayList<>();
        entity.getOrderItemEntities().forEach(itemEntity -> orderItems.add(convertFromEntity(itemEntity)));

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
    private OrderItem convertFromEntity(OrderItemEntity entity) {
        return new OrderItem(
                entity.getItemName(),
                entity.getImageUrl(),
                entity.getQuantity(),
                entity.getUnitPrice()
        );
    }
    private AddressInfo convertFromEntity(AddressInfoEntity entity) {
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
    private OrderEntity convertToEntity(Order order) {
        List<OrderItemEntity> orderItemEntities = new ArrayList<>();
        order.getOrderItems().forEach(orderItem -> orderItemEntities.add(convertToEntity(orderItem)));

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
                .build();
    }
    private OrderItemEntity convertToEntity(OrderItem orderItem) {
        return new OrderItemEntity(
                orderItem.getItemName(),
                orderItem.getImageUrl(),
                orderItem.getQuantity(),
                orderItem.getUnitPrice()
        );
    }
    private AddressInfoEntity convertToEntity(AddressInfo addressInfo) {
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
