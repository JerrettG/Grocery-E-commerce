package com.gonsalves.productservice.integration.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonsalves.productservice.controller.model.ProductCreateRequest;
import com.gonsalves.productservice.controller.model.ProductUpdateRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class Utility {

    private final MockMvc mockMvc;

    private final ObjectMapper mapper;

    public ProductServiceClient productServiceClient;

    public Utility(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        this.mapper = new ObjectMapper();
        this.productServiceClient = new ProductServiceClient();
    }

    public class ProductServiceClient {
        public ResultActions getAllProducts() throws Exception {
            return mockMvc.perform(get("/api/v1/productService/product/all")
                    .accept(MediaType.APPLICATION_JSON));
        }
        public ResultActions getProductByProductName(String productName) throws Exception {
            return mockMvc.perform(get("/api/v1/productService/product/{productName}", productName)
                    .accept(MediaType.APPLICATION_JSON));
        }
        public ResultActions createProduct(ProductCreateRequest request) throws Exception {
            return mockMvc.perform(post("/api/v1/productService/product")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)));
        }
        public ResultActions updateProduct(ProductUpdateRequest request) throws Exception {
            return mockMvc.perform(put("/api/v1/productService/product")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)));
        }
        public ResultActions deleteProduct(String productName) throws Exception{
            return mockMvc.perform(delete("/api/v1/productService/product/{productName}", productName)
                    .accept(MediaType.APPLICATION_JSON));
        }
    }
}
