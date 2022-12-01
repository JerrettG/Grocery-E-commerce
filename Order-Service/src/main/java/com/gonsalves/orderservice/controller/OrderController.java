package com.gonsalves.orderservice.controller;

import com.gonsalves.orderservice.controller.model.*;
import com.gonsalves.orderservice.repository.entity.OrderEntity;
import com.gonsalves.orderservice.exception.OrderAlreadyExistsException;
import com.gonsalves.orderservice.exception.OrderNotFoundException;
import com.gonsalves.orderservice.service.OrderService;
import com.gonsalves.orderservice.service.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/api/v1/orderService")
class OrderController {


    private final OrderService orderService;
    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping(value = "/order/all/user/{userId}")
    public ResponseEntity<OrdersListResponse> getAllOrdersForUserId(@PathVariable String userId) {
        List<Order> orders = orderService.getAllOrdersByUserId(userId);
        List<OrderResponse> orderResponses = new ArrayList<>();
        orders.forEach(order -> orderResponses.add(new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getShippingAddress(),
                order.getOrderTotal(),
                order.getStatus(),
                order.getOrderItems(),
                order.getCreatedDate()
                )
            )
        );

        OrdersListResponse response = new OrdersListResponse(orderResponses);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @GetMapping(value = "/order/{orderId}/user/{userId}")
    public ResponseEntity<OrderResponse> getOrderByOrderId(@PathVariable("orderId") String orderId,
                                                           @PathVariable( "userId") String userId) {
        try {
            Order order = orderService.getOrderByOrderId(userId, orderId);
            OrderResponse response = new OrderResponse(
                    order.getId(),
                    order.getUserId(),
                    order.getShippingAddress(),
                    order.getOrderTotal(),
                    order.getStatus(),
                    order.getOrderItems(),
                    order.getCreatedDate()
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (OrderNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @PostMapping(value = "/order")
    public ResponseEntity<String> createOrder(@RequestBody OrderCreateRequest request){
        try {
            Order order = createOrderFromRequest(request);
            order.setPaymentIntentId(request.getPaymentIntentId());
            orderService.createOrder(order);
            return new ResponseEntity<>("OrderEntity created successfully.", HttpStatus.CREATED);
        } catch (OrderAlreadyExistsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }
    @PutMapping(value = "/order")
    public ResponseEntity<String> updateOrder(@RequestBody OrderUpdateRequest request){
        try {
            Order order = createOrderFromRequest(request);
            order.setId(request.getId());
            orderService.updateOrder(order);
            return new ResponseEntity<>("OrderEntity updated successfully.", HttpStatus.ACCEPTED);
        } catch (OrderNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    @DeleteMapping(value = "/order/{orderId}/user/{userId}")
    public ResponseEntity<String> deleteOrder(@PathVariable("orderId")String orderId,
                                              @PathVariable("userId")String userId){
        try {
            Order order = Order.builder().id(orderId).userId(userId).build();
            orderService.deleteOrder(order);
             return new ResponseEntity<>("OrderEntity deleted successfully.", HttpStatus.ACCEPTED);
        } catch (OrderNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private Order createOrderFromRequest(OrderRequest request) {
        return Order.builder()
                .userId(request.getUserId())
                .shippingAddress(request.getShippingAddress())
                .orderTotal(request.getOrderTotal())
                .status(request.getStatus())
                .orderItems(request.getOrderItems())
                .build();
    }

}
