package com.gonsalves.customerprofileservice.controller.model;

import com.gonsalves.customerprofileservice.service.model.AddressInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileCreateRequest implements CustomerProfileRequest{

    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private AddressInfo shippingInfo;
}
