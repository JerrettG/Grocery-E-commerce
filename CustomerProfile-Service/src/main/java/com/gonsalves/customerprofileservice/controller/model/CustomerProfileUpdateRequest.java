package com.gonsalves.customerprofileservice.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileUpdateRequest implements CustomerProfileRequest{
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String shippingAddress;
}
