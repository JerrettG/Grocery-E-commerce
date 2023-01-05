package com.gonsalves.grocery.frontend.frontend.web;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonsalves.grocery.frontend.frontend.model.*;
import com.gonsalves.grocery.frontend.frontend.model.Product;

import org.bouncycastle.math.raw.Mod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import org.springframework.web.reactive.function.client.WebClient;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


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
    public String home(Model model, Authentication authentication) {
        if (authentication != null) {
            model.addAttribute("isAuthenticated", authentication.isAuthenticated());
            model.addAttribute("userId", authentication.getName());
        }
        return "index";
    }

    @RequestMapping("/product/{productName}")
    public String product(@PathVariable(name = "productName") String productName, Authentication authentication, Model model) {
        Product result = webClient.get()
                                .uri(String.format("/api/v1/productService/product/%s", productName))
                                .retrieve().bodyToMono(Product.class).block();
        model.addAttribute("product", result);
        if (authentication != null) {
            model.addAttribute("isAuthenticated", authentication.isAuthenticated());
            model.addAttribute("userId", authentication.getName());
        }
        return "product";}

    @RequestMapping("/shoppingCart")
    public String shoppingCart(Authentication authentication, Model model) {
        String userId = authentication.getName();
        try {
            model.addAttribute("userId", userId);
        } catch (HttpClientErrorException e) {
            model.addAttribute("error", true);
        }
        return "shoppingCart";
    }

    @GetMapping("/checkout")
    public String checkout(Authentication authentication, Model model) {
        USAStates[] states = USAStates.class.getEnumConstants();
        model.addAttribute("states", states);
        model.addAttribute("stripePublicKey", stripePublicKey);
        model.addAttribute("userId", authentication.getName());
        return "checkout";
    }


    @RequestMapping("/profile")
    public String profile(@AuthenticationPrincipal OidcUser principal, Authentication authentication, Model model) {
        String userId = authentication.getName();
        model.addAttribute("userId", userId);
        return "profile";
    }

    @RequestMapping("/order")
    public String order(@RequestParam("orderId") String orderId, Authentication authentication, Model model) {
        String userId = authentication.getName();
        model.addAttribute("orderId", orderId);
        model.addAttribute("userId", userId);
        return "order";
    }

    @RequestMapping("/success")
    public String success(Authentication authentication, Model model) {
        String userId = authentication.getName();
        model.addAttribute("userId", userId);
        model.addAttribute("stripePublicKey", stripePublicKey);
        return "success";
    }

}
