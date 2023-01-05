package com.gonsalves.grocery.frontend.frontend.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonsalves.grocery.frontend.frontend.model.*;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.issuing.Card;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/payment")
public class PaymentController {
    private final WebClient webClient;
    @Autowired
    public PaymentController(WebClient webClient) {
        this.webClient = webClient;
    }

    @PostMapping("/create-payment-intent")
    public @ResponseBody CreatePaymentResponse createPaymentIntent(@RequestBody CreatePayment createPayment) throws StripeException, JsonProcessingException {

        AddressInfo shippingInfo = createPayment.getShippingInfo();
        PaymentIntentCreateParams.Shipping.Address address = PaymentIntentCreateParams.Shipping.Address.builder()
                .setLine1(shippingInfo.getAddressFirstLine())
                .setLine2(shippingInfo.getAddressSecondLine())
                .setCity(shippingInfo.getCity())
                .setState(shippingInfo.getState())
                .setPostalCode(shippingInfo.getZipCode())
                .setCountry("USA")
                .build();
        PaymentIntentCreateParams.Shipping shipping = PaymentIntentCreateParams.Shipping.builder()
                .setAddress(address)
                .setName(String.format("%s %s", shippingInfo.getFirstName(), shippingInfo.getLastName()))
                .build();
        //TODO change this controller to use an idempotency key allowing only one payment intent to be created per checkout session
        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setCustomer(createPayment.getUserId())
                        .setAmount((long) (createPayment.getTotal()*100))
                        .setCurrency("usd")
                        .setShipping(shipping)
                        .addPaymentMethodType("card")
                        //What shows up on a customer's card statement
                        .setStatementDescriptor("Grocery E-commerce")
                        .build();
        PaymentIntent paymentIntent = PaymentIntent.create(params);

        return new CreatePaymentResponse(paymentIntent.getClientSecret(), paymentIntent.getId());

    }

}