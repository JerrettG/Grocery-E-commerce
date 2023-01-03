package com.gonsalves.grocery.frontend.frontend.payment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gonsalves.grocery.frontend.frontend.model.*;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
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
        ObjectMapper objectMapper = new ObjectMapper();
        Order order = createPayment.getOrder();
        order.setOrderItems(CartItemConverter.convert(createPayment.getItems()));
        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount((long) (order.getTotal()*100))
                        .setCurrency("usd")
                        .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);
        order.setPaymentIntentId(paymentIntent.getId());
        String body = objectMapper.writeValueAsString(order);

        Order response = webClient.post()
                .uri("/api/v1/orderService/order")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(order)
                .exchangeToMono(res -> res.bodyToMono(Order.class))
                .block();
        return new CreatePaymentResponse(paymentIntent.getClientSecret());

    }

}