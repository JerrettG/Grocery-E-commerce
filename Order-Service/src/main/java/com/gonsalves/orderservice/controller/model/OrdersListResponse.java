package com.gonsalves.orderservice.controller.model;

import com.gonsalves.orderservice.repository.entity.OrderEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdersListResponse {

    List<OrderResponse> orderList;

}
