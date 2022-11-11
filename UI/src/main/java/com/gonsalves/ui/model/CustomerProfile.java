package com.gonsalves.UI.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class CustomerProfile {
    private String id;
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String shippingAddress;
    private String creationDate;
}
