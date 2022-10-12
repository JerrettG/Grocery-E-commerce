package com.gonsalves.CartService.service;

import com.gonsalves.CartService.entity.CartItem;
import com.gonsalves.CartService.repository.CartRepository;
import com.gonsalves.ProductService.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
@Slf4j
@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;


    public void addItemToCart(CartItem cartItem) {
        CartItem existingItem = loadItem(cartItem.getUserId(), cartItem.getProductId());
        //TODO fix the loadItem check to see if item exists in the cart because it will always increment
        if (Objects.nonNull(existingItem)) {
            existingItem.incrementItemQuantity(cartItem.getQuantity());
            cartRepository.updateCartItem(existingItem);
            log.info(String.format("Attempted to add item with name %s to cart, but item already exists. Quantity updated.",cartItem.getProductName()));
        }
        else {
            cartRepository.create(cartItem);
            log.info(String.format("Item with name %s added to cart successfully.", cartItem.getProductName()));
        }
    }

    public void updateItemQuantity(CartItem cartItem, int updatedQuantity) {
        cartItem.setQuantity(updatedQuantity);
        cartRepository.updateCartItem(cartItem);
        log.info(String.format("Item with name %s quantity updated successfully to %d", cartItem.getProductName(), cartItem.getQuantity()));
    }

    private CartItem loadItem(String userId, String productId) {
        List<CartItem> results = cartRepository.loadCartItem(userId, productId);
        if (results.size() > 0) return results.get(0);
        return null;
    }

    public List<CartItem> loadAllCartItems(String userId) {
        return cartRepository.loadAllCartItems(userId);
    }

    public void removeItemFromCart(CartItem cartItem) {
        cartRepository.removeItem(cartItem);
        log.info(String.format("Removal of item with name %s successful.", cartItem.getProductName()));
    }
}
