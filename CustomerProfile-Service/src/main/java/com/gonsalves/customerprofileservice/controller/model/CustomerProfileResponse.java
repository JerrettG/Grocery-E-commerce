package com.gonsalves.customerprofileservice.service.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.gonsalves.customerprofileservice.repository.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


public class CustomerProfileResponse {

    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String shippingAddress;
    private String creationDate;
}
