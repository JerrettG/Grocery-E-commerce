package com.gonsalves.customerprofileservice.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerProfile {

    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private AddressInfo shippingInfo;
    private String status;

}
