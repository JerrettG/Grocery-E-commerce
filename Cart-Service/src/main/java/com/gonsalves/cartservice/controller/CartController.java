package com.gonsalves.cartservice.controller;

import com.gonsalves.cartservice.controller.model.AddItemToCartRequest;
import com.gonsalves.cartservice.controller.model.CartResponse;
import com.gonsalves.cartservice.controller.model.RemoveItemFromCartRequest;
import com.gonsalves.cartservice.controller.model.UpdateItemQuantityRequest;
import com.gonsalves.cartservice.exception.CartItemNotFoundException;
import com.gonsalves.cartservice.repository.entity.CartItemEntity;
import com.gonsalves.cartservice.service.model.CartItem;
import com.gonsalves.cartservice.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public @ResponseBody ResponseEntity<String> addCartItem(@RequestBody AddItemToCartRequest request) {
        CartItem cartItem = CartItem.builder()
                .userId(request.getUserId())
                .quantity(Math.abs(request.getQuantity()))
                .productId(request.getProductId())
                .productName(request.getProductName())
                .productImageUrl(request.getProductImageUrl())
                .productPrice(request.getProductPrice())
                .build();
        cartService.addItemToCart(cartItem);
        return new ResponseEntity<>("Adding item to cart was successful.", HttpStatus.CREATED);
    }

    /**
     * This endpoint will update the quantity of a cartItem in the cart of the specified user. If the updated quantity
     * is < 0 the user is met with a 400 status code. If the updated quantity is 0, the item will be removed from the user's
     * cart. If > 0 the quantity will be updated and the user will be returned a 202 status code
     * @param request The request containing the id, userId, and the updated quantity for the item being updated
     * @return A 202 response if quantity >= 0, 400 if not.
     */
    @PutMapping("/cart/{userId}/cartItem")
    public ResponseEntity<String> updateItemQuantity(@RequestBody UpdateItemQuantityRequest request) {
        if (request.getUpdatedQuantity() < 0)
            return ResponseEntity.badRequest().build();
        else if (request.getUpdatedQuantity() == 0) {
            CartItem cartItem = CartItem.builder()
                    .id(request.getId())
                    .userId(request.getUserId())
                    .build();
            cartService.removeItemFromCart(cartItem);
            return new ResponseEntity<>("Item quantity is 0. Item has been removed from cart successfully", HttpStatus.ACCEPTED);
        }
        else {
            try {
                CartItem cartItem = CartItem.builder()
                        .id(request.getId())
                        .userId(request.getUserId())
                        .build();
                cartService.updateItemQuantity(cartItem, request.getUpdatedQuantity());
                return new ResponseEntity<>("Item quantity updated successfully.", HttpStatus.ACCEPTED);
            } catch (CartItemNotFoundException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        }
    }
    @DeleteMapping(path = "/cart/{userId}/cartItem/{cartItemId}")
    public ResponseEntity<String> removeItem(
            @PathVariable("userId")String userId,
            @PathVariable("cartItemId")String cartItemId) {
        CartItem cartItem = CartItem.builder()
                .id(cartItemId)
                .userId(userId)
                .build();
        cartService.removeItemFromCart(cartItem);
        return new ResponseEntity<>("Item removed from cart successfully.", HttpStatus.ACCEPTED);
    }
    @DeleteMapping("/cart/{userId}")
    public ResponseEntity<String> clearCart(@PathVariable("userId") String userId) {
        cartService.clearCart(userId);
        return new ResponseEntity<>("Cart cleared successfully.", HttpStatus.ACCEPTED);
    }

}
