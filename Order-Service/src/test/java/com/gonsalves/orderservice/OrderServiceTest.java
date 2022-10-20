package com.gonsalves.orderservice;

import com.gonsalves.orderservice.entity.Order;
import com.gonsalves.orderservice.entity.OrderItem;
import com.gonsalves.orderservice.exception.OrderAlreadyExistsException;
import com.gonsalves.orderservice.exception.OrderNotFoundException;
import com.gonsalves.orderservice.repository.OrderRepository;
import com.gonsalves.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;
    @Mock
    private OrderRepository orderRepository;

    private Order order;

    private String orderId;

    private String userId;

    private OrderItem orderItem;

    @BeforeEach
    public void before() {
        this.orderId = UUID.randomUUID().toString();
        this.userId = UUID.randomUUID().toString();
        this.orderItem = OrderItem.builder()
                .itemName("Beef Tenderloin")
                .quantity(2)
                .unitPrice(4.99)
                .imageUrl("/demo_images/beefTenderloin.jpg")
                .build();
        this.order = new Order(orderId,userId,UUID.randomUUID().toString(),"1234 Main St, Sacramento, CA, 92222", 23.47, "PROCESSING", new ArrayList<>(), "July 8, 2022");
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getAllOrdersByUserId() {
        List<Order> orderList = new ArrayList<>(Arrays.asList(order));

        when(orderRepository.getAllOrdersByUserId(userId)).thenReturn(orderList);
        List<Order> result = orderService.getAllOrdersByUserId(userId);

        assertEquals(orderList.size(), result.size(), "Expected method to return a list of all orders, but did not");
    }
    @Test
    public void getOrderByOrderId() {

        when(orderRepository.getOrderByOrderId(orderId, userId)).thenReturn(order);

        Order result = orderService.getOrderByOrderId(orderId, userId);

        assertEquals(order.getId(), result. getId(), "Expected result to have matching order id, but did not.");
        assertEquals(order.getPaymentIntentId(), result.getPaymentIntentId(), "Expected result to have matching transaction id, but did not.");
        assertEquals(order.getUserId(), result.getUserId(), "Expected result to have matching user id, but did not.");
    }

    @Test
    public void getOrderByOrderId_orderDoesNotExist_throwsOrderNotFoundException() {

        when(orderRepository.getOrderByOrderId(orderId, userId)).thenReturn(null);

        assertThrows(OrderNotFoundException.class, ()->orderService.getOrderByOrderId(orderId, userId), "Expected method to throw OrderNotFoundException when getting non-existent order, but did not.");
    }

    @Test
    public void createOrder() {
        List<Order> orderList = new ArrayList<>();

        when(orderRepository.getOrderByPaymentIntentId(userId, order.getPaymentIntentId())).thenReturn(orderList);
        orderService.createOrder(order);

        verify(orderRepository).createOrder(order);
    }

    @Test
    public void createOrder_orderAlreadyExists_throwsOrderAlreadyExistsException() {
        List<Order> orderList = new ArrayList<>(Arrays.asList(order));

        when(orderRepository.getOrderByPaymentIntentId(userId, order.getPaymentIntentId())).thenReturn(orderList);

        assertThrows(OrderAlreadyExistsException.class, ()->orderService.createOrder(order),
                "Expected creating an order where an order already exists for transaction id to throw OrderAlreadyExistsException, but did not.");
    }

    @Test
    public void updateOrder() {
        when(orderRepository.getOrderByOrderId(orderId, userId)).thenReturn(order);

        orderService.updateOrder(order);

        verify(orderRepository).updateOrder(order);
    }

    @Test
    public void updateOrder_orderDoesNotExist_throwsOrderNotFoundException() {
        when(orderRepository.getOrderByOrderId(orderId, userId)).thenReturn(null);

        assertThrows(OrderNotFoundException.class, ()->orderService.updateOrder(order),
                "Expected to throw OrderNotFoundException when updating an order that does not exist, but was not.");
    }

    @Test
    public void deleteOrder() {
        when(orderRepository.getOrderByOrderId(orderId, userId)).thenReturn(order);

        orderService.deleteOrder(order);

        verify(orderRepository).deleteOrder(order);
    }

    @Test
    public void deleteOrder_orderDoesNotExist_throwsOrderNotFoundException() {
        when(orderRepository.getOrderByOrderId(orderId, userId)).thenReturn(null);

        assertThrows(OrderNotFoundException.class, ()->orderService.deleteOrder(order),
                "Expected to throw OrderNotFoundException when updating an order that does not exist, but was not.");
    }
}
