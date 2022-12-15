package com.gonsalves.orderservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonsalves.orderservice.controller.model.OrderCreateRequest;
import com.gonsalves.orderservice.controller.model.OrderUpdateRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class Utility {

    private final MockMvc mockMvc;

    private final ObjectMapper mapper;

    public OrderServiceClient orderServiceClient;

    public Utility(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        this.mapper = new ObjectMapper();
        this.orderServiceClient = new OrderServiceClient();
    }

    public class OrderServiceClient {
        public ResultActions getAllOrdersForUserId(String userId) throws Exception {
            return mockMvc.perform(get("/api/v1/orderService/order/all/user/{userId}", userId)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON));
        }
        public ResultActions getOrderByOrderId(String orderId, String userId) throws Exception {
            return mockMvc.perform(get("/api/v1/orderService/order/{orderId}/user/{userId}", orderId, userId)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON));
        }
        public ResultActions createOrder(OrderCreateRequest request) throws Exception {
            return mockMvc.perform(post("/api/v1/orderService/order")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)));
        }
        public ResultActions updateOrder(OrderUpdateRequest request) throws Exception {
            return mockMvc.perform(put("/api/v1/orderService/order")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)));
        }
        public ResultActions deleteOrder(String orderId, String userId) throws Exception{
            return mockMvc.perform(delete("/api/v1/orderService/order/{orderId}/user/{userId}",orderId, userId)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON));
        }
    }
}
