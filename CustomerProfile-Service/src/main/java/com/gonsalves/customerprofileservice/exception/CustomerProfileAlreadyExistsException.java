package com.gonsalves.customerprofileservice.exception;

import com.gonsalves.customerprofileservice.entity.CustomerProfile;

public class CustomerProfileAlreadyExistsException extends RuntimeException{

    public CustomerProfileAlreadyExistsException(String message) {
        super(message);
    }
}
