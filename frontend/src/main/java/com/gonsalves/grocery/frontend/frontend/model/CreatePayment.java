package com.gonsalves.grocery.frontend.frontend.model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePayment {

    private AddressInfo shippingInfo;
    private String userId;
    private Double total;
}
