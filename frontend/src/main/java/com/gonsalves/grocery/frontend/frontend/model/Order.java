package com.gonsalves.grocery.frontend.frontend.model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @SerializedName("userId")
    private String userId;
    @SerializedName("paymentIntentId")
    private String paymentIntentId;
    @SerializedName("shippingInfo")
    private AddressInfo shippingInfo;
    @SerializedName("billingInfo")
    private AddressInfo billingInfo;
    @SerializedName("subtotal")
    private double subtotal;
    @SerializedName("tax")
    private double tax;
    @SerializedName("shippingCost")
    private double shippingCost;
    @SerializedName("total")
    private double total;
    @SerializedName("status")
    private String status;
    @SerializedName("orderItems")
    private List<OrderItem> orderItems;



}