package com.gonsalves.orderservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
        orders.forEach(order -> orderResponses.add(convertToResponse(order)));
        OrdersListResponse response = new OrdersListResponse(orderResponses);
        return ResponseEntity.ok().body(response);
    }
    @GetMapping(value = "/order/{orderId}/user/{userId}")
    public ResponseEntity<OrderResponse> getOrderByOrderId(@PathVariable("orderId") String orderId,
                                                           @PathVariable( "userId") String userId) {
        try {
            Order order = orderService.getOrderByOrderId(userId, orderId);
            OrderResponse response = convertToResponse(order);
            return ResponseEntity.ok().body(response);
        } catch (OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/order/user/{userId}/paymentIntent/{paymentIntentId}")
    public ResponseEntity<OrderResponse> getOrderByPaymentIntentId(
            @PathVariable("paymentIntentId") String paymentIntentId,
            @PathVariable("userId") String userId) {
        try {
            Order order = orderService.getOrderByPaymentIntendId(userId, paymentIntentId);
            OrderResponse response = convertToResponse(order);
            return ResponseEntity.ok().body(response);
        } catch (OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping( "/order")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderCreateRequest request){
        try {
            Order order = new Order();
            order.setUserId(request.getUserId());
            order.setPaymentIntentId(request.getPaymentIntentId());
            order.setShippingInfo(request.getShippingInfo());
            order.setBillingInfo(request.getBillingInfo());
            order.setSubtotal(request.getSubtotal());
            order.setTax(request.getTax());
            order.setShippingCost(request.getShippingCost());
            order.setTotal(request.getTotal());
            order.setStatus(request.getStatus());
            order.setOrderItems(request.getOrderItems());
            Order createdOrder = orderService.createOrder(order);
            OrderResponse response = convertToResponse(createdOrder);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (OrderAlreadyExistsException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
    }
    @PutMapping(value = "/order")
    public ResponseEntity<String> updateOrder(@RequestBody OrderUpdateRequest request){
        try {
            Order order = convertToOrderFromRequest(request);
            order.setId(request.getId());
            orderService.updateOrder(order);
            return new ResponseEntity<>("OrderEntity updated successfully.", HttpStatus.ACCEPTED);
        } catch (OrderNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
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
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    private Order convertToOrderFromRequest(OrderRequest request) {
        return Order.builder()
                .userId(request.getUserId())
                .shippingInfo(request.getShippingInfo())
                .total(request.getTotal())
                .status(request.getStatus())
                .orderItems(request.getOrderItems())
                .build();
    }

    private OrderResponse convertToResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getShippingInfo(),
                order.getBillingInfo(),
                order.getSubtotal(),
                order.getTax(),
                order.getShippingCost(),
                order.getTotal(),
                order.getStatus(),
                order.getOrderItems(),
                order.getCreatedDate()
        );
    }

}
