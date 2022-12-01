package com.gonsalves.ui.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    private String itemName;
    private String imageUrl;
    private int quantity;
    private double unitPrice;
}
