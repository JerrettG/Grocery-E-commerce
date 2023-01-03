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
    private List<CartItem> items;
    private Order order;

}
