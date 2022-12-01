package com.gonsalves.cartservice.unit;

import com.gonsalves.cartservice.repository.entity.CartItemEntity;
import com.gonsalves.cartservice.repository.CartRepository;
import com.gonsalves.cartservice.service.model.CartItem;
import com.gonsalves.cartservice.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Mockito.*;


public class CartServiceTest {

    @InjectMocks
    private CartService cartService;
    @Mock
    private CartRepository cartRepository;

    private CartItemEntity cartItemEntity;
    private CartItem cartItem;
    private String id;
    private String userId;
    private String productId;
    private String productName;
    private String productImageUrl;
    private double productPrice;
    @BeforeEach
    protected void setup() {
        id = UUID.randomUUID().toString();
        userId = UUID.randomUUID().toString();
        productId = UUID.randomUUID().toString();
        productName = "Beef Tenderloin";
        productImageUrl = "/demo_images/beefTenderloin.jpg";
        productPrice = 4.99;
        cartItem = CartItem.builder()
                .id(id)
                .userId(userId)
                .productId(productId)
                .quantity(1)
                .productName(productName)
                .productPrice(productPrice)
                .productImageUrl(productImageUrl)
                .build();
        this.cartItemEntity = CartItemEntity.builder()
                .userId(userId)
                .productId(productId)
                .quantity(1)
                .productName(productName)
                .productPrice(productPrice)
                .productImageUrl(productImageUrl)
                .build();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void addItemToCart_itemNotInCart_AddsItemToCart() {

        when(cartRepository.loadCartItem(eq(userId), eq(productId))).thenReturn(new ArrayList<>());

        cartService.addItemToCart(cartItem);

        verify(cartRepository).create(eq(cartItemEntity));
    }
    @Test
    public void addItemToCart_itemAlreadyInCart_itemQuantityIncremented() {

        when(cartRepository.loadCartItem(userId, productId)).thenReturn(Arrays.asList(cartItemEntity));
        cartService.addItemToCart(cartItem);

        verify(cartRepository).updateCartItem(cartItemEntity);
    }
}
