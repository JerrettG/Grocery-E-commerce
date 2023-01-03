package com.gonsalves.cartservice.controller;

import com.gonsalves.cartservice.controller.model.*;
import com.gonsalves.cartservice.exception.CartItemNotFoundException;
import com.gonsalves.cartservice.repository.entity.CartItemEntity;
import com.gonsalves.cartservice.service.model.CartItem;
import com.gonsalves.cartservice.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.UUID.randomUUID;

@RestController("CartController")
@RequestMapping("/api/v1/cartService")
public class CartController {


    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }


    @GetMapping("/cart/{userId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable("userId") String userId) {
        List<CartItem> cartItems = cartService.loadAllCartItems(userId);
        CartResponse cartResponse = new CartResponse();
        cartResponse.setCartItems(cartItems);
        cartResponse.calculateTotalCost();
        return new ResponseEntity<>(cartResponse, HttpStatus.OK);
    }

    @PostMapping("/cart/{userId}/cartItem")
    public @ResponseBody ResponseEntity<CartItemResponse> addCartItem(@RequestBody AddItemToCartRequest request) {
        CartItem cartItem = CartItem.builder()
                .userId(request.getUserId())
                .quantity(Math.abs(request.getQuantity()))
                .productId(request.getProductId())
                .productName(request.getProductName())
                .productImageUrl(request.getProductImageUrl())
                .productPrice(request.getProductPrice())
                .build();
        cartService.addItemToCart(cartItem);
        CartItemResponse response = CartItemResponse.builder()
                .id(cartItem.getId())
                .userId(cartItem.getUserId())
                .quantity(cartItem.getQuantity())
                .productName(cartItem.getProductName())
                .productPrice(cartItem.getProductPrice())
                .productImageUrl(cartItem.getProductImageUrl())
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * This endpoint will update the quantity of a cartItem in the cart of the specified user. If the updated quantity
     * is < 0 the user is met with a 400 status code. If the updated quantity is 0, the item will be removed from the user's
     * cart. If > 0 the quantity will be updated and the user will be returned a 202 status code
     * @param request The request containing the id, userId, and the updated quantity for the item being updated
     * @return A 202 response if quantity >= 0, 400 if not.
     */
    @PutMapping("/cart/{userId}/cartItem")
    public ResponseEntity<CartResponse> updateItemQuantity(@RequestBody UpdateItemQuantityRequest request) {
        if (request.getUpdatedQuantity() < 0)
            return ResponseEntity.badRequest().build();
        else if (request.getUpdatedQuantity() == 0) {
            CartItem cartItem = CartItem.builder()
                    .id(request.getId())
                    .userId(request.getUserId())
                    .build();
            cartService.removeItemFromCart(cartItem);
            return ResponseEntity.accepted().build();
        }
        else {
            try {
                CartItem cartItem = CartItem.builder()
                        .id(request.getId())
                        .userId(request.getUserId())
                        .build();
                cartService.updateItemQuantity(cartItem, request.getUpdatedQuantity());

                return ResponseEntity.accepted().build();
            } catch (CartItemNotFoundException e) {
                return ResponseEntity.notFound().build();
            }
        }
    }
    @DeleteMapping(path = "/cart/{userId}/cartItem/{cartItemId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable("userId")String userId,
            @PathVariable("cartItemId")String cartItemId) {
        CartItem cartItem = CartItem.builder()
                .id(cartItemId)
                .userId(userId)
                .build();
        cartService.removeItemFromCart(cartItem);
        return ResponseEntity.accepted().build();
    }
    @DeleteMapping("/cart/{userId}")
    public ResponseEntity<CartResponse> clearCart(@PathVariable("userId") String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.accepted().build();
    }

}
