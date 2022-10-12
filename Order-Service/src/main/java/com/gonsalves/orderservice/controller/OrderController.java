package com.gonsalves.orderservice.controller;

import com.gonsalves.orderservice.entity.Order;
import com.gonsalves.orderservice.entity.OrdersList;
import com.gonsalves.orderservice.exception.OrderNotFoundException;
import com.gonsalves.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orderService")
class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/order")
    public @ResponseBody OrdersList getAllOrdersForUserId(@RequestParam(required = true) String userId) {
        OrdersList orders = new OrdersList(orderService.getAllOrdersByUserId(userId));
        return orders;
    }
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Order> getOrderByOrderId(@RequestParam(required = true, value = "userId") String userId, @PathVariable("orderId") String orderId) {
        try {
            Order order = orderService.getOrderByOrderId(orderId, userId);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (OrderNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @PostMapping("/order")
    public ResponseEntity<String> createOrder(Order order){
        orderService.createOrder(order);
        return new ResponseEntity<>("Order created successfully.", HttpStatus.CREATED);
    }
    @PutMapping("/order")
    public ResponseEntity<String> updateOrder(Order order){
        try {
            orderService.updateOrder(order);
            return new ResponseEntity<>("Order updated successfully.", HttpStatus.ACCEPTED);
        } catch (OrderNotFoundException e) {
            return new ResponseEntity<>("Order could not be updated because order could not be found.", HttpStatus.BAD_REQUEST);
        }
    }
    @DeleteMapping("/order")
    public ResponseEntity<String> deleteOrder(Order order){
        try {
            orderService.deleteOrder(order);
             return new ResponseEntity<>("Order deleted successfully.", HttpStatus.ACCEPTED);
        } catch (OrderNotFoundException e) {
            return new ResponseEntity<>("Order could not be deleted because order could not be found.", HttpStatus.BAD_REQUEST);
        }
    }

}
