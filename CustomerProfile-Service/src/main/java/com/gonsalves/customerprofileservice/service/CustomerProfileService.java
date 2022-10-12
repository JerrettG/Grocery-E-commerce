package com.gonsalves.customerprofileservice.service;

import com.gonsalves.customerprofileservice.entity.CustomerProfile;
import com.gonsalves.customerprofileservice.exception.CustomerProfileAlreadyExistsException;
import com.gonsalves.customerprofileservice.exception.CustomerProfileNotFoundException;
import com.gonsalves.customerprofileservice.repository.CustomerProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerProfileService {
    @Autowired
    CustomerProfileRepository customerProfileRepository;
    public CustomerProfile loadCustomerByUserId(String userId) {
      CustomerProfile customerProfile = customerProfileRepository.loadCustomerByUserId(userId);
      if (customerProfile == null)
          throw new CustomerProfileNotFoundException("Customer with specified userId does not exist.");
      return customerProfile;
    }

    public void createCustomerProfile(CustomerProfile profile) {
        try {
            profile.setId(null);
            loadCustomerByUserId(profile.getUserId());
            throw new CustomerProfileAlreadyExistsException("Customer with specified userId already exists.");
        } catch (CustomerProfileNotFoundException e) {
            customerProfileRepository.createCustomerProfile(profile);
        }
    }

    public void updateCustomerProfile(CustomerProfile updatedProfile) {
        CustomerProfile previousProfile = loadCustomerByUserId(updatedProfile.getUserId());
        updatedProfile.setId(previousProfile.getId());
        customerProfileRepository.updateCustomerProfile(updatedProfile);
    }

    public void deleteCustomerProfile(String userId) {
        CustomerProfile profile =  loadCustomerByUserId(userId);
        customerProfileRepository.deleteCustomerProfile(profile);
    }
}
