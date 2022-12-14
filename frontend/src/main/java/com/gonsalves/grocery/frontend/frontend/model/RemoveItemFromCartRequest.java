package com.gonsalves.grocery.frontend.frontend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RemoveItemFromCartRequest {

    private String id;
    private String userId;
}
