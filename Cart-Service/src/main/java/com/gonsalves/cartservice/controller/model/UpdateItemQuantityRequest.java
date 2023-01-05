package com.gonsalves.cartservice.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateItemQuantityRequest {

    private String id;
    private String userId;
    private Integer updatedQuantity;

}
