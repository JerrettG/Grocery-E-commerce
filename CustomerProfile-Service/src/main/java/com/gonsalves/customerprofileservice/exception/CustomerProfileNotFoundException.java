package com.gonsalves.customerprofileservice.exception;

public class CustomerProfileNotFoundException extends RuntimeException{

    public CustomerProfileNotFoundException(String message) {
        super(message);
    }
}
