package com.gonsalves.customerprofileservice.exception;

public class CustomerProfileAlreadyExistsException extends RuntimeException{

    public CustomerProfileAlreadyExistsException(String message) {
        super(message);
    }
}
