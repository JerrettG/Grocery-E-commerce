package com.gonsalves.orderservice.service;

import com.gonsalves.orderservice.config.CacheStore;
import com.gonsalves.orderservice.exception.OrderAlreadyExistsException;
import com.gonsalves.orderservice.exception.OrderNotFoundException;
import com.gonsalves.orderservice.repository.OrderRepository;
import com.gonsalves.orderservice.repository.entity.OrderEntity;
import com.gonsalves.orderservice.repository.entity.OrderItemEntity;
import com.gonsalves.orderservice.repository.entity.Status;
import com.gonsalves.orderservice.service.model.Order;
import com.gonsalves.orderservice.service.model.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
                .forEach(entity -> ordersFromDatabase.add(createOrderFromEntity(entity)));
        cache.add(userId, ordersFromDatabase);

        return ordersFromDatabase;
    }


    public Order getOrderByOrderId(String userId, String orderId) {
        OrderEntity entity = orderRepository.getOrderByOrderId(userId, orderId);
        if (entity == null)
            throw new OrderNotFoundException("Order with order id could not be found.");
        return createOrderFromEntity(entity);
    }
    public void createOrder(Order order) {
        List<OrderEntity> results = orderRepository.getOrderByPaymentIntentId(order.getUserId(), order.getPaymentIntentId());
        if (results.size() > 0)
            throw new OrderAlreadyExistsException("Cannot create order. Order already created for this transaction.");
        orderRepository.createOrder(createEntityFromOrder(order));
        cache.evict(order.getUserId());
    }

    public void updateOrder(Order order) {
        getOrderByOrderId(order.getUserId(), order.getId());

        orderRepository.updateOrder(createEntityFromOrder(order));
        cache.evict(order.getUserId());
    }

    public void cancelOrder(Order order) {
        OrderEntity entity = createEntityFromOrder(order);
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
    private Order createOrderFromEntity(OrderEntity entity) {
        List<OrderItem> orderItems = new ArrayList<>();
        entity.getOrderItemEntities().forEach(itemEntity -> orderItems.add(createOrderItemFromEntity(itemEntity)));

        return Order.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .paymentIntentId(entity.getPaymentIntentId())
                .shippingAddress(entity.getShippingAddress())
                .orderTotal(entity.getOrderTotal())
                .status(entity.getStatus().toString())
                .orderItems(orderItems)
                .createdDate(entity.getCreatedDate())
                .build();


    }
    private OrderItem createOrderItemFromEntity(OrderItemEntity entity) {
        return new OrderItem(
                entity.getItemName(),
                entity.getImageUrl(),
                entity.getQuantity(),
                entity.getUnitPrice()
        );

    }
    private OrderEntity createEntityFromOrder(Order order) {
        List<OrderItemEntity> orderItemEntities = new ArrayList<>();
        order.getOrderItems().forEach(orderItem -> orderItemEntities.add(createEntityFromOrderItem(orderItem)));

        return OrderEntity.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .paymentIntentId(order.getPaymentIntentId())
                .shippingAddress(order.getShippingAddress())
                .orderTotal(order.getOrderTotal())
                .status(Status.valueOf(order.getStatus()))
                .orderItemEntities(orderItemEntities)
                .build();

    }
    private OrderItemEntity createEntityFromOrderItem(OrderItem orderItem) {
        return new OrderItemEntity(
                orderItem.getItemName(),
                orderItem.getImageUrl(),
                orderItem.getQuantity(),
                orderItem.getUnitPrice()
        );
    }
}
