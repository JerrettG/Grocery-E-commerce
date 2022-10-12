package com.gonsalves.UI.payment;

import com.gonsalves.CartService.entity.CartItem;
import com.google.gson.annotations.SerializedName;

public class CreatePayment {
    @SerializedName("items")
    CartItem[] items;

    public CartItem[] getItems() {
        return items;
    }
}
