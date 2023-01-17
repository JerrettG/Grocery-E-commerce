package com.gonsalves.grocery.frontend.frontend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

    private String userId;
    private String name;
    private String email;
    private Double total;
}
