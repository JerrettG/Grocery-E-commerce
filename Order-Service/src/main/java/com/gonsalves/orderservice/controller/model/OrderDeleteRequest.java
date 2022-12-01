package com.gonsalves.orderservice.controller.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDeleteRequest {
    private String id;
    private String userId;
}
