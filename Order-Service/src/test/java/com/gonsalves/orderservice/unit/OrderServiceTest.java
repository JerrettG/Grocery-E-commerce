package com.gonsalves.orderservice.unit;

import com.gonsalves.orderservice.repository.entity.OrderEntity;
import com.gonsalves.orderservice.repository.entity.OrderItemEntity;
import com.gonsalves.orderservice.exception.OrderAlreadyExistsException;
import com.gonsalves.orderservice.exception.OrderNotFoundException;
import com.gonsalves.orderservice.repository.OrderRepository;
import com.gonsalves.orderservice.service.OrderService;
import com.gonsalves.orderservice.service.model.Order;
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

public class OrderEntityServiceTest {

    @InjectMocks
    private OrderService orderService;
    @Mock
    private OrderRepository orderRepository;
    private Order order;

    private OrderEntity orderEntity;

    private String orderId;

    private String userId;

    private OrderItemEntity orderItemEntity;

    @BeforeEach
    public void before() {
        this.orderId = UUID.randomUUID().toString();
        this.userId = UUID.randomUUID().toString();
        this.orderItemEntity = new OrderItemEntity(
                "Beef Tenderloin",
                "/demo_images/beefTenderloin.jpg",
                2,
                4.99
        );
        this.order = new Order(orderId, userId, UUID.randomUUID().toString(), "1234 Main St, Sacramento, CA, 92222", 23.47, "PROCESSING", new ArrayList<>(), "July 8, 2022");
        this.orderEntity = new OrderEntity(orderId,userId,UUID.randomUUID().toString(),"1234 Main St, Sacramento, CA, 92222", 23.47, "PROCESSING", new ArrayList<>(), "July 8, 2022");
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getAllOrdersByUserId() {
        List<OrderEntity> orderEntityList = new ArrayList<>(Arrays.asList(orderEntity));

        when(orderRepository.getAllOrdersByUserId(userId)).thenReturn(orderEntityList);
        List<Order> result = orderService.getAllOrdersByUserId(userId);

        assertEquals(orderEntityList.size(), result.size(), "Expected method to return a list of all orders, but did not");
    }
    @Test
    public void getOrderByOrderId() {

        when(orderRepository.getOrderByOrderId(orderId, userId)).thenReturn(orderEntity);

        Order result = orderService.getOrderByOrderId(orderId, userId);

        assertEquals(orderEntity.getId(), result. getId(), "Expected result to have matching orderEntity id, but did not.");
        assertEquals(orderEntity.getPaymentIntentId(), result.getPaymentIntentId(), "Expected result to have matching transaction id, but did not.");
        assertEquals(orderEntity.getUserId(), result.getUserId(), "Expected result to have matching user id, but did not.");
    }

    @Test
    public void getOrderByOrderId_orderDoesNotExist_throwsOrderNotFoundException() {

        when(orderRepository.getOrderByOrderId(orderId, userId)).thenReturn(null);

        assertThrows(OrderNotFoundException.class, ()->orderService.getOrderByOrderId(orderId, userId), "Expected method to throw OrderNotFoundException when getting non-existent orderEntity, but did not.");
    }

    @Test
    public void createOrder() {
        List<OrderEntity> orderEntityList = new ArrayList<>();

        when(orderRepository.getOrderByPaymentIntentId(userId, orderEntity.getPaymentIntentId())).thenReturn(orderEntityList);
        orderService.createOrder(order);

        verify(orderRepository).createOrder(orderEntity);
    }

    @Test
    public void createOrder_orderAlreadyExists_throwsOrderAlreadyExistsException() {
        List<OrderEntity> orderEntityList = new ArrayList<>(Arrays.asList(orderEntity));

        when(orderRepository.getOrderByPaymentIntentId(userId, orderEntity.getPaymentIntentId())).thenReturn(orderEntityList);

        assertThrows(OrderAlreadyExistsException.class, ()->orderService.createOrder(order),
                "Expected creating an orderEntity where an orderEntity already exists for transaction id to throw OrderAlreadyExistsException, but did not.");
    }

    @Test
    public void updateOrder() {
        when(orderRepository.getOrderByOrderId(orderId, userId)).thenReturn(orderEntity);

        orderService.updateOrder(order);

        verify(orderRepository).updateOrder(orderEntity);
    }

    @Test
    public void updateOrder_orderDoesNotExist_throwsOrderNotFoundException() {
        when(orderRepository.getOrderByOrderId(orderId, userId)).thenReturn(null);

        assertThrows(OrderNotFoundException.class, ()->orderService.updateOrder(order),
                "Expected to throw OrderNotFoundException when updating an orderEntity that does not exist, but was not.");
    }

    @Test
    public void deleteOrder() {
        when(orderRepository.getOrderByOrderId(orderId, userId)).thenReturn(orderEntity);

        orderService.deleteOrder(order);

        verify(orderRepository).deleteOrder(orderEntity);
    }

    @Test
    public void deleteOrder_orderDoesNotExist_throwsOrderNotFoundException() {
        when(orderRepository.getOrderByOrderId(orderId, userId)).thenReturn(null);

        assertThrows(OrderNotFoundException.class, ()->orderService.deleteOrder(order),
                "Expected to throw OrderNotFoundException when updating an orderEntity that does not exist, but was not.");
    }
}
