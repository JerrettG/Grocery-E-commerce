package com.gonsalves.cartservice.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonsalves.cartservice.controller.model.AddItemToCartRequest;
import com.gonsalves.cartservice.controller.model.UpdateItemQuantityRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class Utility {

    private final MockMvc mockMvc;

    private final ObjectMapper mapper;

    public CartServiceClient cartServiceClient;

    public Utility(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        this.mapper = new ObjectMapper();
        this.cartServiceClient = new CartServiceClient();
    }

    public class CartServiceClient {
        public ResultActions getCart(String userId) throws Exception {
            return mockMvc.perform(get("/api/v1/cartService/cart/{userId}", userId)
                    .accept(MediaType.APPLICATION_JSON)
            );
        }
        public ResultActions addCartItem(AddItemToCartRequest request) throws Exception {
            return mockMvc.perform(post("/api/v1/cartService/cart/{userId}/cartItem", request.getUserId())
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)));
        }
        public ResultActions updateItemQuantity(UpdateItemQuantityRequest request) throws Exception {
            return mockMvc.perform(put("/api/v1/cartService/cart/{userId}/cartItem", request.getUserId())
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)));
        }
        public ResultActions removeItem(String userId, String itemId) throws Exception{
            return mockMvc.perform(delete("/api/v1/cartService/cart/{userId}/cartItem/{cartItemId}", userId, itemId)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON));
        }
        public ResultActions clearCart(String userId) throws Exception {
            return mockMvc.perform(delete("/api/v1/cartService/cart/{userId}",userId)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON));
        }
    }
}
