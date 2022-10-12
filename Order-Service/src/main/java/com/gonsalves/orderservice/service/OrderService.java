package com.gonsalves.orderservice.service;

import com.gonsalves.orderservice.entity.Order;
import com.gonsalves.orderservice.exception.OrderNotFoundException;
import com.gonsalves.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public List<Order> getAllOrdersByUserId(String userId) {
        return orderRepository.getAllOrdersByUserId(userId);
    }

    public Order getOrderByOrderId(String orderId, String userId) {
        Order order = orderRepository.getOrderByOrderId(orderId, userId);
        if (order == null)
            throw new OrderNotFoundException("Order with order id could not be found.");
        return order;
    }

    public void createOrder(Order order) {
     orderRepository.createOrder(order);
    }

    public void updateOrder(Order order) {
//        orderRepository.getOrderByOrderId(order.getId());

        orderRepository.updateOrder(order);
    }

    public void deleteOrder(Order order) {
//        orderRepository.getOrderByOrderId(order.getId());

        orderRepository.deleteOrder(order);
    }

}
