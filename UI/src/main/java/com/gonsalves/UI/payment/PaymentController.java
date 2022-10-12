package com.gonsalves.UI.payment;

import com.gonsalves.CartService.entity.CartItem;
import com.google.gson.Gson;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @PostMapping("/create-payment-intent")
    public @ResponseBody CreatePaymentResponse createPaymentIntent(@RequestBody CreatePayment createPayment) throws StripeException {

        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(calculateOrderAmount(createPayment.getItems()))
                        .setCurrency("usd")
                        .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        return new CreatePaymentResponse(paymentIntent.getClientSecret());

    }

    private long calculateOrderAmount(CartItem[] items) {
        CartItem[] cartItems = items;
        long total =0;
        for (CartItem item : cartItems)  total+= ( (item.getProductPrice()*100) * item.getQuantity() );
        return total;
    }
}