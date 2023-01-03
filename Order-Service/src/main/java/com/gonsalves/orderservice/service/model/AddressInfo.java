package com.gonsalves.orderservice.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressInfo {

    private String firstName;
    private String lastName;
    private String addressFirstLine;
    private String addressSecondLine;
    private String city;
    private String state;
    private String zipCode;
}