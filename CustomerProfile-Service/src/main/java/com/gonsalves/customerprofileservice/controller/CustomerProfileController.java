package com.gonsalves.customerprofileservice.controller;

import com.gonsalves.customerprofileservice.entity.CustomerProfile;
import com.gonsalves.customerprofileservice.exception.CustomerProfileAlreadyExistsException;
import com.gonsalves.customerprofileservice.exception.CustomerProfileNotFoundException;
import com.gonsalves.customerprofileservice.repository.CustomerProfileRepository;
import com.gonsalves.customerprofileservice.service.CustomerProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customerProfileService")
public class CustomerProfileController {

    @Autowired
    CustomerProfileService customerProfileService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<CustomerProfile> getCustomerProfileByUserId(@PathVariable("userId") String userId) {
        try {
            CustomerProfile customerProfile = customerProfileService.loadCustomerByUserId(userId);
            return new ResponseEntity<>(customerProfile, HttpStatus.OK);
        } catch (CustomerProfileNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(path="/user",  consumes = "application/xml;charset=UTF-8", produces = "application/xml;charset=UTF-8")
    public ResponseEntity<String> createCustomerProfile(@RequestBody CustomerProfile profile) {
        try {
            if (profile.getId().isEmpty())
                profile.setId(null);
            customerProfileService.createCustomerProfile(profile);
            return new ResponseEntity<>("Customer profile created successfully.", HttpStatus.CREATED);
        } catch (CustomerProfileAlreadyExistsException e) {
            return new ResponseEntity<>("Cannot create profile. Customer with userId already exists.", HttpStatus.CONFLICT);
        }
    }


    @PutMapping("/user")
    public ResponseEntity<String> updateCustomerProfile(@RequestBody CustomerProfile profile) {
        try {
            customerProfileService.updateCustomerProfile(profile);
            return new ResponseEntity<>("Customer profile updated successfully.", HttpStatus.ACCEPTED);
        } catch (CustomerProfileNotFoundException e) {
            return new ResponseEntity<>("Cannot update profile. Customer specified userId does not exist.", HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<String> deleteCustomerProfile(@PathVariable("userId") String userId) {
        try {
            customerProfileService.deleteCustomerProfile(userId);
            return new ResponseEntity<>("Customer profile deleted successfully.", HttpStatus.ACCEPTED);
        } catch (CustomerProfileNotFoundException e) {
            return new ResponseEntity<>("Cannot delete profile. Customer with specified userId does not exist.", HttpStatus.BAD_REQUEST);
        }
    }

}
