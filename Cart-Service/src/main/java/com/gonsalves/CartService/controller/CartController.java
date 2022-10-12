package com.gonsalves.CartService.controller;

import com.gonsalves.CartService.entity.Cart;
import com.gonsalves.CartService.entity.CartItem;
import com.gonsalves.CartService.service.CartService;
import com.gonsalves.ProductService.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("CartController")
@RequestMapping("/api/v1/cartService")
public class CartController {

    @Autowired
    private CartService cartService;


    @GetMapping("/cart/{userId}")
    public ResponseEntity<Cart> getCart(@PathVariable("userId") String userId) {
        List<CartItem> cartItems = cartService.loadAllCartItems(userId);
        Cart cart = new Cart();
        cart.setCartItems(cartItems);
        cart.calculateTotalCost();
        return new ResponseEntity<>(cart, HttpStatus.OK);
    }

    @PostMapping(path = "/cartItem", consumes = "application/xml;charset=UTF-8", produces = "application/xml;charset=UTF-8")
    public @ResponseBody ResponseEntity<String> addCartItem(@RequestBody CartItem cartItem) {
        //DynamoDB will not autogenerate hash key if it is not null
        if (cartItem.getId().isEmpty()) cartItem.setId(null);
        try {
            cartService.addItemToCart(cartItem);
            return new ResponseEntity<>("Adding item to cart was successful.", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error adding item to cart.", HttpStatus.CONFLICT);
        }
    }

    @PutMapping("/cartItem")
    public ResponseEntity<String> updateItemQuantity(@RequestBody CartItem cartItem, @RequestParam("quantity") int updatedQuantity ) {
        cartService.updateItemQuantity(cartItem, updatedQuantity);
        return new ResponseEntity<>("Item quantity updated successfully.", HttpStatus.ACCEPTED);
    }

    @DeleteMapping(path = "/cartItem", consumes = "application/xml;charset=UTF-8", produces = "application/xml;charset=UTF-8")
    public ResponseEntity<String> removeItem(@RequestBody CartItem cartItem) {
        cartService.removeItemFromCart(cartItem);
        return new ResponseEntity<>("Item removed from cart successfully.", HttpStatus.OK);
    }
}
