package com.gonsalves.UI.web;


import com.gonsalves.CartService.entity.Cart;
import com.gonsalves.CartService.entity.CartItem;
import com.gonsalves.ProductService.entity.Product;
import com.gonsalves.ProductService.entity.ProductList;

import com.gonsalves.customerprofileservice.entity.CustomerProfile;
import com.gonsalves.orderservice.entity.Order;
import com.gonsalves.orderservice.entity.OrderItem;
import com.gonsalves.orderservice.entity.OrdersList;
import org.ietf.jgss.Oid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Controller("UIController")
public class UIController {

    @Value("${stripe.public.key}")
    private String stripePublicKey;

    private final RestTemplate restTemplate = new RestTemplate();
    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @RequestMapping({"/", "/home"})
    public String home(Model model) {
        ProductList response = restTemplate.getForObject(getBaseUrl("PRODUCT_SERVICE") + "/api/v1/productService/allProducts", ProductList.class);
        if (response.getProductList() != null)
            model.addAttribute("products", response.getProductList());
        else
            model.addAttribute("products", new ArrayList<Product>());
        return "index";
    }

    @RequestMapping("/product/{productName}")
    public String product(@PathVariable(name = "productName") String productName, Model model) {
        Product result = restTemplate.getForObject(String.format("%s/api/v1/productService/%s", getBaseUrl("PRODUCT_SERVICE"),productName), Product.class);
        model.addAttribute("product", result);
        model.addAttribute("cartItem", new CartItem());
        return "product";}

    @RequestMapping("/shoppingCart")
    public String shoppingCart(Authentication authentication, Model model) {
        String userId = authentication.getName();
        Cart cart = restTemplate.getForObject(String.format("%s/api/v1/cartService/cart/%s",getBaseUrl("CART_SERVICE"), userId), Cart.class);
        model.addAttribute("cart", cart);
        return "shoppingCart";}

    @RequestMapping(path = "/product/{productName}/addToCart")
    public String addToCart(@ModelAttribute("cartItem") CartItem cartItem, @RequestParam(name = "quantity") int quantity,
                          Authentication authentication) {
        cartItem.setQuantity(quantity);
        cartItem.setUserId(authentication.getName());
        ResponseEntity<String> response = restTemplate.postForEntity(getBaseUrl("CART_SERVICE") + "/api/v1/cartService/cartItem", cartItem, String.class);
        return String.format("redirect:/product/%s",cartItem.getProductName());

    }
    @PostMapping(path = "/shoppingCart")
    public String removeFromCart(@ModelAttribute("cartItem") CartItem cartItem, Authentication authentication, Model model) {
        ResponseEntity<String> response = restTemplate.exchange(getBaseUrl("CART_SERVICE") + "/api/v1/cartService/cartItem", HttpMethod.DELETE, new HttpEntity<CartItem>(cartItem), String.class);
        return shoppingCart(authentication, model);
    }
    //TODO Find a way for this method to obtain cart items without making another call to cartService
    @RequestMapping(value = "/checkout")
    public String checkout(Authentication authentication, Model model, HttpServletRequest request) {
        String userId = authentication.getName();
        Cart cart = restTemplate.getForObject(String.format("%s/api/v1/cartService/cart/%s",getBaseUrl("CART_SERVICE"), userId), Cart.class);

        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");

        model.addAttribute("csrf", token.getToken());
        model.addAttribute("stripePublicKey", stripePublicKey);
        model.addAttribute("cartItems", cart.getCartItems().toArray());
        return "checkout";
    }

    @RequestMapping("/profile")
    public String profile(@AuthenticationPrincipal OidcUser principal, Authentication authentication, Model model) {
        String userId = authentication.getName();
        CustomerProfile profile;
        try {
            profile = restTemplate.getForObject(String.format("%s/api/v1/customerProfileService/user/%s", getBaseUrl("CUSTOMER_PROFILE_SERVICE"), userId), CustomerProfile.class);
            if (profile != null)
                model.addAttribute("profile", profile);
        } catch (HttpClientErrorException e) {
            profile = new CustomerProfile();
            profile.setUserId(userId);
            profile.setEmail(principal.getEmail());
            ResponseEntity<String> response = restTemplate.postForEntity(String.format("%s/api/v1/customerProfileService/user", getBaseUrl("CUSTOMER_PROFILE_SERVICE")), profile, String.class);
            model.addAttribute("profile", profile);
        }
        OrdersList orders = restTemplate.getForObject(String.format("%s/api/v1/orderService/order?userId=%s", getBaseUrl("ORDER_SERVICE"), authentication.getName()), OrdersList.class);
        System.out.println(orders);
        model.addAttribute("orders", orders.getOrderList());
        return "profile";
    }

    @RequestMapping("/success")
    public String success() {
        return "success";
    }

    private String getBaseUrl(String serviceName) {
        ServiceInstance instance = loadBalancerClient.choose(serviceName);
        return instance.getUri().toString();
    }

}
