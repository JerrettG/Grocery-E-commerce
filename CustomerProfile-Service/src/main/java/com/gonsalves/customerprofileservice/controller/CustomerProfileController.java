package com.gonsalves.customerprofileservice.controller;

import brave.Response;
import com.gonsalves.customerprofileservice.controller.model.CustomerProfileCreateRequest;
import com.gonsalves.customerprofileservice.controller.model.CustomerProfileRequest;
import com.gonsalves.customerprofileservice.controller.model.CustomerProfileResponse;
import com.gonsalves.customerprofileservice.controller.model.CustomerProfileUpdateRequest;
import com.gonsalves.customerprofileservice.repository.entity.CustomerProfileEntity;
import com.gonsalves.customerprofileservice.exception.CustomerProfileAlreadyExistsException;
import com.gonsalves.customerprofileservice.exception.CustomerProfileNotFoundException;
import com.gonsalves.customerprofileservice.service.CustomerProfileService;
import com.gonsalves.customerprofileservice.service.model.CustomerProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customerProfileService")
public class CustomerProfileController {


    private final CustomerProfileService customerProfileService;
    @Autowired
    public CustomerProfileController(CustomerProfileService customerProfileService) {
        this.customerProfileService = customerProfileService;
    }
    @GetMapping(value = "/user/{userId}")
    public ResponseEntity<CustomerProfileResponse> getCustomerProfileByUserId(@PathVariable("userId") String userId) {
        try {
            CustomerProfile customerProfile = customerProfileService.loadCustomerByUserId(userId);
            CustomerProfileResponse response = convertToResponse(customerProfile);
            return ResponseEntity.ok().body(response);
        } catch (CustomerProfileNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(path="/user")
    public ResponseEntity<String> createCustomerProfile(@RequestBody CustomerProfileCreateRequest request) {
        try {
            customerProfileService.createCustomerProfile(convertToProfile(request));
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (CustomerProfileAlreadyExistsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }


    @PutMapping("/user")
    public ResponseEntity<String> updateCustomerProfile(@RequestBody CustomerProfileUpdateRequest request) {
        try {
            customerProfileService.updateCustomerProfile(convertToProfile(request));
            return ResponseEntity.accepted().build();
        } catch (CustomerProfileNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<String> deleteCustomerProfile(
            @RequestParam(name = "eraseData", defaultValue = "false") boolean eraseData,
            @PathVariable("userId") String userId) {
        if (eraseData) {
            try {
                customerProfileService.deleteCustomerProfile(userId);
                return new ResponseEntity<>("Customer data erased successfully.", HttpStatus.ACCEPTED);
            } catch (CustomerProfileNotFoundException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
            }
        }
        else {
            try {
                customerProfileService.deactivateCustomerProfile(userId);
                return new ResponseEntity<>("Customer profile deleted successfully.", HttpStatus.ACCEPTED);
            } catch (CustomerProfileNotFoundException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
            }
        }
    }

    private CustomerProfileResponse convertToResponse(CustomerProfile profile) {
        return new CustomerProfileResponse(
                profile.getEmail(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getShippingAddress(),
                profile.getStatus());
    } 
    
    private CustomerProfile convertToProfile(CustomerProfileRequest request) {
        return CustomerProfile.builder()
                .userId(request.getUserId())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .shippingAddress(request.getShippingAddress())
                .build();
    }

}
