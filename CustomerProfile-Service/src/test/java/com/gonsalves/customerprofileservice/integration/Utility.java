package com.gonsalves.customerprofileservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonsalves.customerprofileservice.controller.model.CustomerProfileCreateRequest;
import com.gonsalves.customerprofileservice.controller.model.CustomerProfileUpdateRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class Utility {

    private final MockMvc mockMvc;

    private final ObjectMapper mapper;

    public CustomerProfileServiceClient customProfileServiceClient;

    public Utility(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        this.mapper = new ObjectMapper();
        this.customProfileServiceClient = new CustomerProfileServiceClient();
    }

    public class CustomerProfileServiceClient {
        public ResultActions getCustomerProfileByUserId(String userId) throws Exception {
            return mockMvc.perform(get("/api/v1/customerProfileService/user/{userId}", userId)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON));
        }
        public ResultActions createCustomerProfile(CustomerProfileCreateRequest request) throws Exception {
            return mockMvc.perform(post("/api/v1/customerProfileService/user")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)));
        }
        public ResultActions updateCustomerProfile(CustomerProfileUpdateRequest request) throws Exception {
            return mockMvc.perform(put("/api/v1/customerProfileService/user")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)));
        }
        public ResultActions deleteCustomerProfile(boolean eraseData, String userId) throws Exception{
            return mockMvc.perform(delete("/api/v1/customerProfileService/user/{userId}", userId)
                    .param("eraseData", String.valueOf(eraseData))
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON));
        }
    }
}
