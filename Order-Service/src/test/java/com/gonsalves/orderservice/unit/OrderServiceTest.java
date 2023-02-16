package com.gonsalves.orderservice.unit;

import com.gonsalves.orderservice.caching.DistributedCache;
import com.gonsalves.orderservice.caching.InMemoryCache;
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
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private DistributedCache cache;
    private Order order;

    private OrderEntity orderEntity;

    private String orderId;

    private String userId;
    private String paymentIntentId;

    private OrderItemEntity orderItemEntity;
    private final String ORDERS_FOR_USER_KEY ="Orders-UserId::%s";

    @BeforeEach
    public void before() {
        this.order = Util.createOrder();
        this.orderEntity = Util.createEntity(this.order);
        this.orderId = this.order.getId();
        this.userId = this.order.getUserId();
        this.paymentIntentId = this.order.getPaymentIntentId();
    }

    @Test
    public void getAllOrdersByUserId() {
        List<OrderEntity> orderEntityList = new ArrayList<>(Arrays.asList(orderEntity));

        when(orderRepository.getAllOrdersByUserId(userId)).thenReturn(orderEntityList);
        when(cache.getValue(eq(String.format(ORDERS_FOR_USER_KEY, userId)))).thenReturn(Optional.empty());
        List<Order> result = orderService.getAllOrdersByUserId(userId);

        assertEquals(orderEntityList.size(), result.size(), "Expected method to return a list of all orders, but did not");
    }
    @Test
    public void getOrderByOrderId() {

        when(orderRepository.getOrderByOrderId(userId, orderId)).thenReturn(Optional.ofNullable(orderEntity));
        Order result = orderService.getOrderByOrderId(userId, orderId);

        assertEquals(orderEntity.getId(), result. getId(), "Expected result to have matching orderEntity id, but did not.");
        assertEquals(orderEntity.getPaymentIntentId(), result.getPaymentIntentId(), "Expected result to have matching transaction id, but did not.");
        assertEquals(orderEntity.getUserId(), result.getUserId(), "Expected result to have matching user id, but did not.");
    }

    @Test
    public void getOrderByOrderId_orderDoesNotExist_throwsOrderNotFoundException() {

        when(orderRepository.getOrderByOrderId(userId, orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, ()->orderService.getOrderByOrderId(userId, orderId), "Expected method to throw OrderNotFoundException when getting non-existent orderEntity, but did not.");
    }

    @Test
    public void createOrder() {

        when(orderRepository.getOrderByPaymentIntentId(userId, orderEntity.getPaymentIntentId())).thenReturn(Optional.empty());
        orderService.createOrder(order);

        verify(orderRepository).createOrder(any(OrderEntity.class));
    }

    @Test
    public void createOrder_orderAlreadyExists_throwsOrderAlreadyExistsException() {

        when(orderRepository.getOrderByPaymentIntentId(userId, orderEntity.getPaymentIntentId())).thenReturn(Optional.ofNullable(orderEntity));

        assertThrows(OrderAlreadyExistsException.class, ()->orderService.createOrder(order),
                "Expected creating an orderEntity where an orderEntity already exists for transaction id to throw OrderAlreadyExistsException, but did not.");
    }

    @Test
    public void updateOrder() {
        when(orderRepository.getOrderByOrderId(userId, orderId)).thenReturn(Optional.ofNullable(orderEntity));

        orderService.updateOrder(order);

        verify(orderRepository).updateOrder(any(OrderEntity.class));
    }

    @Test
    public void updateOrder_orderDoesNotExist_throwsOrderNotFoundException() {
        when(orderRepository.getOrderByOrderId(userId, orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, ()->orderService.updateOrder(order),
                "Expected to throw OrderNotFoundException when updating an orderEntity that does not exist, but was not.");
    }

    @Test
    public void deleteOrder() {
        when(orderRepository.getOrderByOrderId(userId, orderId)).thenReturn(Optional.ofNullable(orderEntity));

        orderService.deleteOrder(order);

        verify(orderRepository).deleteOrder(any(OrderEntity.class));
    }

    @Test
    public void deleteOrder_orderDoesNotExist_throwsOrderNotFoundException() {
        when(orderRepository.getOrderByOrderId(userId, orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, ()->orderService.deleteOrder(order),
                "Expected to throw OrderNotFoundException when updating an orderEntity that does not exist, but was not.");
    }
}
