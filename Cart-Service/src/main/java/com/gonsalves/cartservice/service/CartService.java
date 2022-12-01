package com.gonsalves.cartservice.service;

import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.gonsalves.cartservice.controller.model.UpdateItemQuantityRequest;
import com.gonsalves.cartservice.exception.CartItemNotFoundException;
import com.gonsalves.cartservice.repository.entity.CartItemEntity;
import com.gonsalves.cartservice.repository.CartRepository;
import com.gonsalves.cartservice.service.model.CartItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
@Slf4j
@Service
public class CartService {


    private final CartRepository cartRepository;
    @Autowired
    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public void addItemToCart(CartItem cartItem) {
        CartItemEntity existingItem = loadItem(cartItem.getUserId(), cartItem.getProductId());
        if (Objects.nonNull(existingItem)) {
            int updatedQuantity = existingItem.getQuantity() + cartItem.getQuantity();
            existingItem.setQuantity(updatedQuantity);
            cartRepository.updateCartItem(existingItem);
            log.info(String.format("Attempted to add item with name %s to cart, but item already exists. Quantity updated.", cartItem.getProductName()));
        }
        else {
            CartItemEntity entity = CartItemEntity.builder()
                    .userId(cartItem.getUserId())
                    .quantity(cartItem.getQuantity())
                    .productId(cartItem.getProductId())
                    .productName(cartItem.getProductName())
                    .productImageUrl(cartItem.getProductImageUrl())
                    .productPrice(cartItem.getProductPrice())
                    .build();
            cartRepository.create(entity);
            log.info(String.format("Item with name %s added to cart successfully.", entity.getProductName()));
        }
    }

    public void updateItemQuantity(CartItem cartItem, int updatedQuantity) {
        CartItemEntity entity = CartItemEntity.builder()
                .id(cartItem.getId())
                .userId(cartItem.getUserId())
                .quantity(updatedQuantity).build();
        try {
            cartRepository.updateCartItem(entity);
        } catch (ConditionalCheckFailedException e) {
            throw new CartItemNotFoundException("Cannot update item quantity that is not in cart.");
        }
    }

    private CartItemEntity loadItem(String userId, String productId) {
        List<CartItemEntity> results = cartRepository.loadCartItem(userId, productId);
        if (results.size() > 0)
            return results.get(0);
        return null;
    }

    public List<CartItem> loadAllCartItems(String userId) {
        List<CartItem> cartItems = new ArrayList<>();
        List<CartItemEntity> results = cartRepository.loadAllCartItems(userId);
        results.forEach(entity -> cartItems.add(convertToItem(entity)));

        return cartItems;
    }


    public void removeItemFromCart(CartItem cartItem) {
        cartRepository.removeItem(convertToEntity(cartItem));
        log.info(String.format("Removal of item with name %s successful.", cartItem.getProductName()));
    }

    public void clearCart(String userId) {
        cartRepository.clearCart(userId);
    }

    private CartItem convertToItem(CartItemEntity entity) {
        return CartItem.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .quantity(entity.getQuantity())
                .productId(entity.getProductId())
                .productName(entity.getProductName())
                .productImageUrl(entity.getProductImageUrl())
                .productPrice(entity.getProductPrice())
                .build();
    }

    private CartItemEntity convertToEntity(CartItem cartItem) {
        return CartItemEntity.builder()
                .id(cartItem.getId())
                .userId(cartItem.getUserId())
                .quantity(cartItem.getQuantity())
                .productId(cartItem.getProductId())
                .productName(cartItem.getProductName())
                .productImageUrl(cartItem.getProductImageUrl())
                .productPrice(cartItem.getProductPrice())
                .build();
    }
}
