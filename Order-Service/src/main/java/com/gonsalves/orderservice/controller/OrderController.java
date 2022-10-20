package com.gonsalves.orderservice.controller;

import com.gonsalves.orderservice.entity.Order;
import com.gonsalves.orderservice.entity.OrdersList;
import com.gonsalves.orderservice.exception.OrderAlreadyExistsException;
import com.gonsalves.orderservice.exception.OrderNotFoundException;
import com.gonsalves.orderservice.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/orderService")
class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping(value = "/order")
    public @ResponseBody OrdersList getAllOrdersForUserId(@RequestParam(required = true, value = "userId") String userId) {
        OrdersList orders = new OrdersList(orderService.getAllOrdersByUserId(userId));
        return orders;
    }
    @GetMapping(value = "/order/{orderId}",  consumes = "application/xml;charset=UTF-8", produces = "application/xml;charset=UTF-8")
    public ResponseEntity<Order> getOrderByOrderId(@RequestParam(required = true, value = "userId") String userId, @PathVariable("orderId") String orderId) {
        try {
            Order order = orderService.getOrderByOrderId(orderId, userId);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (OrderNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @PostMapping(value = "/order",  consumes = "application/xml;charset=UTF-8", produces = "application/xml;charset=UTF-8")
    public ResponseEntity<String> createOrder(@RequestBody Order order){
        try {
            orderService.createOrder(order);
            return new ResponseEntity<>("Order created successfully.", HttpStatus.CREATED);
        } catch (OrderAlreadyExistsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }
    @PutMapping(value = "/order",  consumes = "application/xml;charset=UTF-8", produces = "application/xml;charset=UTF-8")
    public ResponseEntity<String> updateOrder(@RequestBody Order order){
        try {
            orderService.updateOrder(order);
            return new ResponseEntity<>("Order updated successfully.", HttpStatus.ACCEPTED);
        } catch (OrderNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    @DeleteMapping(value = "/order",  consumes = "application/xml;charset=UTF-8", produces = "application/xml;charset=UTF-8")
    public ResponseEntity<String> deleteOrder(@RequestBody Order order){
        try {
            orderService.deleteOrder(order);
             return new ResponseEntity<>("Order deleted successfully.", HttpStatus.ACCEPTED);
        } catch (OrderNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
