package com.gonsalves.orderservice.service;

import com.gonsalves.orderservice.util.TypeConverter;
import com.gonsalves.orderservice.caching.DistributedCache;
import com.gonsalves.orderservice.exception.OrderAlreadyExistsException;
import com.gonsalves.orderservice.exception.OrderNotFoundException;
import com.gonsalves.orderservice.repository.OrderRepository;
import com.gonsalves.orderservice.repository.entity.OrderEntity;
import com.gonsalves.orderservice.repository.entity.Status;
import com.gonsalves.orderservice.service.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private static final String ORDERS_FOR_USER_KEY = "Orders-UserId::%s";
    private final OrderRepository orderRepository;
    private final DistributedCache distributedCache;


    @Autowired
    public OrderService(OrderRepository orderRepository, DistributedCache distributedCache) {
        this.orderRepository = orderRepository;
        this.distributedCache = distributedCache;
    }

    public List<Order> getAllOrdersByUserId(String userId) {
        String key = String.format(ORDERS_FOR_USER_KEY, userId);
        Optional<String> cachedOrders = distributedCache.getValue(key);
        if (cachedOrders.isPresent())
            return TypeConverter.fromJsonToOrderList(cachedOrders.get());

        List<Order> ordersFromDatabase = orderRepository.getAllOrdersByUserId(userId).stream()
                .map(TypeConverter::convertFromEntity)
                .collect(Collectors.toList());

        addToDistributedCache(key, ordersFromDatabase);

        return ordersFromDatabase;
    }


    public Order getOrderByOrderId(String userId, String orderId) {
        OrderEntity entity = orderRepository.getOrderByOrderId(userId, orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with order id could not be found."));
        return TypeConverter.convertFromEntity(entity);
    }

    public Order getOrderByPaymentIntendId(String userId, String paymentIntentId) {
        OrderEntity entity = orderRepository.getOrderByPaymentIntentId(userId, paymentIntentId)
                .orElseThrow(() -> new OrderNotFoundException("Order with payment id could not be found."));
        return TypeConverter.convertFromEntity(entity);
    }

    public Order createOrder(Order order) {
        orderRepository.getOrderByPaymentIntentId(order.getUserId(), order.getPaymentIntentId())
                .ifPresent(existingOrder -> {
                    throw new OrderAlreadyExistsException("Cannot create order. Order already created for this transaction.");
                });

        order.setId(UUID.randomUUID().toString());
        order.setCreatedDate(LocalDateTime.now().toString());
        orderRepository.createOrder(TypeConverter.convertToEntity(order));

        String key = String.format(ORDERS_FOR_USER_KEY, order.getUserId());
        distributedCache.invalidate(key);

        return order;
    }

    public void updateOrder(Order order) {
        getOrderByOrderId(order.getUserId(), order.getId());
        orderRepository.updateOrder(TypeConverter.convertToEntity(order));

        String key = String.format(ORDERS_FOR_USER_KEY, order.getUserId());
        distributedCache.invalidate(key);
    }

    public void cancelOrder(Order order) {
        OrderEntity entity = TypeConverter.convertToEntity(order);
        entity.setStatus(Status.CANCELLED);
        orderRepository.updateOrder(entity);

        String key = String.format(ORDERS_FOR_USER_KEY, order.getUserId());
        distributedCache.invalidate(key);
    }

    public void deleteOrder(Order order) {
        getOrderByOrderId(order.getUserId(), order.getId());
        orderRepository.deleteOrder(OrderEntity.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .build());

        String key = String.format(ORDERS_FOR_USER_KEY, order.getUserId());
        distributedCache.invalidate(key);
    }

    private void addToDistributedCache(String key, List<Order> orders) {
        distributedCache.setValue(key, 60*60, TypeConverter.toJson(orders));
    }


}
