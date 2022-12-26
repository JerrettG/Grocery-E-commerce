package com.gonsalves.grocery.frontend.frontend.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonsalves.grocery.frontend.frontend.model.*;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @PostMapping("/create-payment-intent")
    public @ResponseBody CreatePaymentResponse createPaymentIntent(@RequestBody CreatePayment createPayment, @RequestParam String customerId) throws StripeException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Order order = CartItemConverter.convert(createPayment.getItems(), customerId);
        order.calculateAndSetOrderTotal();
        String json = mapper.writeValueAsString(order);

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(calculateOrderAmount(createPayment.getItems()))
                        .setDescription(json)
                        .setCurrency("usd")
                        .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        return new CreatePaymentResponse(paymentIntent.getClientSecret());

    }

    private long calculateOrderAmount(List<CartItem> cartItems) {
        long total =0;
        for (CartItem item : cartItems)  total+= ( (item.getProductPrice()*100) * item.getQuantity() );
        return total;
    }
}