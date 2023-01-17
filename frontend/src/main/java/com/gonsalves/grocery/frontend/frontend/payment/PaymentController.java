package com.gonsalves.grocery.frontend.frontend.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gonsalves.grocery.frontend.frontend.model.*;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.CustomerSearchResult;
import com.stripe.model.PaymentIntent;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerSearchParams;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;


@RestController
@RequestMapping("/payment")
public class PaymentController {
    private final WebClient webClient;
    @Value("${stripe.api.key}")
    private String stripeApiKey;
    @Autowired
    public PaymentController(WebClient  webClient) {
        this.webClient = webClient;
    }

    @PostMapping("/create-payment-intent")
    public @ResponseBody CreatePaymentResponse createPaymentIntent(@RequestBody CreatePaymentRequest createPaymentRequest) throws StripeException, JsonProcessingException {
        Stripe.apiKey = stripeApiKey;
        String queryString = String.format("metadata['userId']:'%s'", createPaymentRequest.getUserId());
        CustomerSearchParams searchParams =
                CustomerSearchParams
                        .builder()
                        .setQuery(queryString)
                        .build();
        CustomerSearchResult result = Customer.search(searchParams);
        Customer customer;

        if (result.getData().size() == 0) {
            CustomerCreateParams customerCreateParams = CustomerCreateParams.builder()
                    .setName(createPaymentRequest.getName())
                    .setEmail(createPaymentRequest.getEmail())
                    .putMetadata("userId", createPaymentRequest.getUserId())
                    .build();
            customer = Customer.create(customerCreateParams);
        } else {
            customer = result.getData().get(0);
        }
        //TODO change this controller to use an idempotency key allowing only one payment intent to be created per checkout session
        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setCustomer(customer.getId())
                        .setAmount((long) (createPaymentRequest.getTotal()*100))
                        .setCurrency("usd")
                        .addPaymentMethodType("card")
                        //What shows up on a customer's card statement
                        .setStatementDescriptor("Grocery E-commerce")
                        .build();
        PaymentIntent paymentIntent = PaymentIntent.create(params);

        return new CreatePaymentResponse(paymentIntent.getClientSecret(), paymentIntent.getId());

    }

    @PutMapping("/update-payment-intent")
    public @ResponseBody UpdatePaymentResponse updatePaymentIntent(@RequestBody UpdatePaymentRequest updatePayment) {



        return new UpdatePaymentResponse();
    }

}