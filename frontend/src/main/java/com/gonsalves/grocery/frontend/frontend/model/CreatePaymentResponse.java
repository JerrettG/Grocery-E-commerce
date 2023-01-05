package com.gonsalves.grocery.frontend.frontend.model;

public class CreatePaymentResponse {
    private String clientSecret;
    private String paymentIntentId;
    public CreatePaymentResponse(String clientSecret, String paymentIntentId) {

        this.clientSecret = clientSecret;
        this.paymentIntentId = paymentIntentId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }
}
