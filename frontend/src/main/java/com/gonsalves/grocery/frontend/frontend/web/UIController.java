package com.gonsalves.grocery.frontend.frontend.web;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonsalves.grocery.frontend.frontend.model.*;
import com.gonsalves.grocery.frontend.frontend.model.Product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;



@Controller
public class UIController {


    private final WebClient webClient;

    @Value("${stripe.public.key}")
    private String stripePublicKey;
    @Autowired
    public UIController(WebClient webClient) {
        this.webClient = webClient;
    }

    @RequestMapping({"/", "/home"})
    public String home(Model model,
                       @RequestParam(value = "category", defaultValue = "")String category,
                       @RequestParam(value = "searchTerm", defaultValue = "")String searchTerm, Authentication authentication) {
        ProductList response;
        if (category.isBlank() && searchTerm.isBlank())
            response =  webClient.get()
                    .uri("/api/v1/productService/product/all")
                    .retrieve().bodyToMono(ProductList.class).block();
        else if (!category.isBlank())
            response = webClient.get()
                    .uri(String.format("/api/v1/productService/product/all?category=%s", category))
                    .retrieve().bodyToMono(ProductList.class).block();
        else
            response = webClient.get()
                    .uri(String.format("/api/v1/productService/product/all?searchTerm=%s", searchTerm))
                    .retrieve().bodyToMono(ProductList.class).block();

        if (response != null && response.getProductList() != null)
            model.addAttribute("products", response.getProductList());
        else
            model.addAttribute("products", new ArrayList<Product>());
        if (authentication != null)
            model.addAttribute("isAuthenticated", authentication.isAuthenticated());
        return "index";
    }

    @RequestMapping("/product/{productName}")
    public String product(@PathVariable(name = "productName") String productName, Model model) {
        Product result = webClient.get()
                                .uri(String.format("/api/v1/productService/product/%s", productName))
                                .retrieve().bodyToMono(Product.class).block();
        model.addAttribute("product", result);
        model.addAttribute("cartItem", new CartItem());
        return "product";}

    @RequestMapping("/shoppingCart")
    public String shoppingCart(Authentication authentication, Model model) {
        String userId = authentication.getName();
        try {
            Cart cart = webClient.get()
                            .uri(String.format("/api/v1/cartService/cart/%s", userId))
                                    .retrieve().bodyToMono(Cart.class).block();
            model.addAttribute("cart", cart);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", true);
        }
        return "shoppingCart";
    }

    @RequestMapping(path = "/product/{productName}/addToCart")
    public String addToCart(@ModelAttribute("cartItem") CartItem cartItem, @RequestParam(name = "quantity") int quantity,
                            Authentication authentication) {
        String userId = authentication.getName();
        cartItem.setUserId(userId);
        cartItem.setQuantity(quantity);
        ResponseEntity<String> response = webClient.post()
                .uri(String.format("/api/v1/cartService/cart/%s/cartItem", userId))
                .body(Mono.just(cartItem), CartItem.class).retrieve().toEntity(String.class).block();
        return String.format("redirect:/product/%s",cartItem.getProductName());

    }
    @PostMapping(path = "/shoppingCart")
    public ResponseEntity<String> removeFromCart(@RequestBody CartItem cartItem, Authentication authentication, Model model) {
        ResponseEntity<String> response = webClient
                .delete()
                .uri(String.format("/api/v1/cartService/cart/%s/cartItem/%s", cartItem.getUserId(),cartItem.getId()))
                .retrieve().toEntity(String.class).block();
        return response;
    }

    @PostMapping("/checkout")
    public String checkout(Authentication authentication, String cartString, Model model) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Cart cart = objectMapper.readValue(cartString, Cart.class);

        model.addAttribute("stripePublicKey", stripePublicKey);
        model.addAttribute("cartItems", cart.getCartItems().toArray());
        model.addAttribute("customerId", authentication.getName());
        return "checkout";
    }

    @GetMapping("/checkout")
    public String checkout() {
        return "checkout";
    }



    @RequestMapping("/profile")
    public String profile(HttpServletRequest request, @AuthenticationPrincipal OidcUser principal, Authentication authentication, Model model) {
        String userId = authentication.getName();
        CustomerProfile profile;
        try {
            profile = webClient.get()
                            .uri(String.format("/api/v1/customerProfileService/user/%s", userId))
                            .retrieve().bodyToMono(CustomerProfile.class).block();
            if (profile != null)
                model.addAttribute("profile", profile);
        } catch (HttpClientErrorException e) {

            profile = new CustomerProfile();
            profile.setUserId(userId);
            profile.setEmail(principal.getEmail());
            ResponseEntity<String> response = webClient.post()
                    .uri("/api/v1/customerProfileService/user")
                    .body(Mono.just(profile), CustomerProfile.class)
                    .retrieve().toEntity(String.class).block();
            model.addAttribute("profile", profile);
        }
        OrdersList orders = webClient.get()
                        .uri(String.format("/api/v1/orderService/order/all/user/%s", userId))
                        .retrieve().bodyToMono(OrdersList.class).block();
        model.addAttribute("orders", orders.getOrderList());
        return "profile";
    }

    @RequestMapping("/success")
    public String success() {
        return "success";
    }

}