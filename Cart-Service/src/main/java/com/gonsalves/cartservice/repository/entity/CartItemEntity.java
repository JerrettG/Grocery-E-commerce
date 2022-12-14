package com.gonsalves.cartservice.repository.entity;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DynamoDBTable(tableName = "Ecommerce-CartService-CartItems")
public class CartItemEntity {
    public static final String PRODUCT_ID_INDEX = "product_id-index";
    @DynamoDBHashKey(attributeName = "user_id")
    private String userId;
    @DynamoDBRangeKey(attributeName = "item_id")
    private String id;

    @DynamoDBAttribute(attributeName = "quantity")
    private Integer quantity;
    @DynamoDBIndexRangeKey(localSecondaryIndexName = PRODUCT_ID_INDEX, attributeName = "product_id")
    //LSI loads keys only
    private String productId;

    @DynamoDBAttribute(attributeName = "product_name")
    private String productName;
    @DynamoDBAttribute(attributeName = "product_image_url")
    private String productImageUrl;
    @DynamoDBAttribute(attributeName = "product_price")
    private Double productPrice;


    @Override
    public String toString() {
        return "CartItemEntity{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", quantity=" + quantity +
                ", productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", productImageUrl='" + productImageUrl + '\'' +
                ", productPrice=" + productPrice +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItemEntity cartItemEntity = (CartItemEntity) o;
        return Objects.equals(id, cartItemEntity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
