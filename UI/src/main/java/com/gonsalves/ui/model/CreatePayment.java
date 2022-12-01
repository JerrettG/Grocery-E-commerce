package com.gonsalves.ui.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CreatePayment {
    @SerializedName("items")
    private List<CartItem> items;

    public List<CartItem> getItems() {
        return items;
    }
}
