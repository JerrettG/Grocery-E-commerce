package com.gonsalves.UI.web;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonsalves.UI.model.Cart;
import com.gonsalves.UI.model.CartItem;
import com.gonsalves.UI.model.Product;
import com.gonsalves.UI.model.ProductList;
import com.gonsalves.UI.model.CustomerProfile;
import com.gonsalves.UI.model.OrdersList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
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
import java.util.ArrayList;

@Slf4j
@Controller("UIController")
public class UIController {

    @Value("${stripe.public.key}")
    private String stripePublicKey;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @RequestMapping({"/", "/home"})
    public String home(Model model) {
        ProductList response = restTemplate.getForObject("http://API-GATEWAY/api/v1/productService/allProducts", ProductList.class);
        if (response.getProductList() != null)
            model.addAttribute("products", response.getProductList());
        else
            model.addAttribute("products", new ArrayList<Product>());
        return "index";
    }

    @RequestMapping("/product/{productName}")
    public String product(@PathVariable(name = "productName") String productName, Model model) {
        Product result = restTemplate.getForObject(String.format("http://API-GATEWAY/api/v1/productService/%s", productName), Product.class);
        model.addAttribute("product", result);
        model.addAttribute("cartItem", new CartItem());
        return "product";}

    @RequestMapping("/shoppingCart")
    public String shoppingCart(Authentication authentication, Model model) {
        String userId = authentication.getName();
        try {
            Cart cart = restTemplate.getForObject(String.format("http://API-GATEWAY/api/v1/cartService/cart/%s", userId), Cart.class);
            model.addAttribute("cart", cart);
        } catch (HttpClientErrorException e) {
            log.warn(e.getMessage());
            model.addAttribute("error", true);
        }
        return "shoppingCart";}

    @RequestMapping(path = "/product/{productName}/addToCart")
    public String addToCart(@ModelAttribute("cartItem") CartItem cartItem, @RequestParam(name = "quantity") int quantity,
                          Authentication authentication) {
        cartItem.setQuantity(quantity);
        cartItem.setUserId(authentication.getName());
        ResponseEntity<String> response = restTemplate.postForEntity("http://API-GATEWAY/api/v1/cartService/cartItem", cartItem, String.class);
        return String.format("redirect:/product/%s",cartItem.getProductName());

    }
    @PostMapping(path = "/shoppingCart")
    public String removeFromCart(@ModelAttribute("cartItem") CartItem cartItem, Authentication authentication, Model model) {
        ResponseEntity<String> response = restTemplate.exchange( "http://API-GATEWAY/api/v1/cartService/cartItem", HttpMethod.DELETE, new HttpEntity<CartItem>(cartItem), String.class);
        return shoppingCart(authentication, model);
    }

    @PostMapping("/checkout")
    public String checkout(Authentication authentication, String cartString, Model model, HttpServletRequest request) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Cart cart = objectMapper.readValue(cartString, Cart.class);
        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");

        model.addAttribute("csrf", token.getToken());
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
            profile = restTemplate.getForObject(String.format("http://API-GATEWAY/api/v1/customerProfileService/user/%s", userId), CustomerProfile.class);
            if (profile != null)
                model.addAttribute("profile", profile);
        } catch (HttpClientErrorException e) {
            log.warn(e.getMessage());

            profile = new CustomerProfile();
            profile.setUserId(userId);
            profile.setEmail(principal.getEmail());
            ResponseEntity<String> response = restTemplate.postForEntity("http://API-GATEWAY/api/v1/customerProfileService/user", profile, String.class);

            model.addAttribute("profile", profile);
        }
        OrdersList orders = restTemplate.getForObject(String.format("http://API-GATEWAY/api/v1/orderService/order?userId=%s", authentication.getName()), OrdersList.class);
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
