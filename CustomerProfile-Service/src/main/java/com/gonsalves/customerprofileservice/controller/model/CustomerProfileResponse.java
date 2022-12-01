package com.gonsalves.customerprofileservice.controller.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.gonsalves.customerprofileservice.repository.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileResponse {

    private String email;
    private String firstName;
    private String lastName;
    private String shippingAddress;
    private String status;
}
