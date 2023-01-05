package com.gonsalves.customerprofileservice.service;

import com.gonsalves.customerprofileservice.config.CacheStore;
import com.gonsalves.customerprofileservice.controller.model.CustomerProfileCreateRequest;
import com.gonsalves.customerprofileservice.controller.model.CustomerProfileRequest;
import com.gonsalves.customerprofileservice.controller.model.CustomerProfileUpdateRequest;
import com.gonsalves.customerprofileservice.repository.entity.AddressInfoEntity;
import com.gonsalves.customerprofileservice.repository.entity.CustomerProfileEntity;
import com.gonsalves.customerprofileservice.exception.CustomerProfileAlreadyExistsException;
import com.gonsalves.customerprofileservice.exception.CustomerProfileNotFoundException;
import com.gonsalves.customerprofileservice.repository.CustomerProfileRepository;
import com.gonsalves.customerprofileservice.repository.entity.Status;
import com.gonsalves.customerprofileservice.service.model.AddressInfo;
import com.gonsalves.customerprofileservice.service.model.CustomerProfile;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerProfileService {
    private final CustomerProfileRepository customerProfileRepository;
    private final CacheStore cache;
    @Autowired
    public CustomerProfileService(CustomerProfileRepository customerProfileRepository, CacheStore cache) {
        this.customerProfileRepository = customerProfileRepository;
        this.cache = cache;
    }
    public CustomerProfile loadCustomerByUserId(String userId) {
        CustomerProfile cachedProfile = cache.get(userId);
        if (cachedProfile != null)
            return cachedProfile;

      CustomerProfileEntity entity = customerProfileRepository.loadCustomerByUserId(userId);
      if (entity == null)
          throw new CustomerProfileNotFoundException("Customer with specified userId does not exist.");

      CustomerProfile profileFromDataDatabase = convertToCustomerProfile(entity);
      cache.add(userId, profileFromDataDatabase);

      return profileFromDataDatabase;
    }

    public void createCustomerProfile(CustomerProfile profile) {
        try {
            CustomerProfile retrievedProfile = loadCustomerByUserId(profile.getUserId());
            if (retrievedProfile.getStatus().equals(Status.ACTIVE.toString()))
                throw new CustomerProfileAlreadyExistsException("Customer with specified userId already exists.");
            else {
                CustomerProfileEntity entity = convertToEntity(retrievedProfile);
                entity.setStatus(Status.ACTIVE);
                customerProfileRepository.updateCustomerProfile(entity);
            }
        } catch (CustomerProfileNotFoundException e) {
            CustomerProfileEntity entity = convertToEntity(profile);
            entity.setStatus(Status.ACTIVE);
            customerProfileRepository.createCustomerProfile(entity);
        }
    }

    public void updateCustomerProfile(CustomerProfile profile) {
        CustomerProfile previousProfile = loadCustomerByUserId(profile.getUserId());
        CustomerProfileEntity updatedProfile = convertToEntity(profile);

        customerProfileRepository.updateCustomerProfile(updatedProfile);
        cache.evict(profile.getUserId());
    }

    /**
     * This method performs a hard delete of the customer's profile information 
     * @param userId The userId associated with the profile being deleted
     */
    public void deleteCustomerProfile(String userId) {
        CustomerProfile profile =  loadCustomerByUserId(userId);
        CustomerProfileEntity entity = CustomerProfileEntity.builder().userId(userId).build();

        customerProfileRepository.deleteCustomerProfile(entity);
        cache.evict(userId);
    }

    /**
     * Performs a soft delete of the customer's profile by retaining their info, but setting
     * their status to Inactive.
     * @param userId The userId associated with the profile being deactivated
     */
    public void deactivateCustomerProfile(String userId) {
        CustomerProfile profile =  loadCustomerByUserId(userId);

        CustomerProfileEntity entity = CustomerProfileEntity.builder()
                .userId(profile.getUserId())
                .email(profile.getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .shippingInfo(convertToEntity(profile.getShippingInfo()))
                .build();
        entity.setStatus(Status.INACTIVE);
        customerProfileRepository.updateCustomerProfile(entity);
        cache.evict(userId);
    }

    private CustomerProfile convertToCustomerProfile(CustomerProfileEntity entity) {
        return new CustomerProfile(
                entity.getUserId(),
                entity.getEmail(),
                entity.getFirstName(),
                entity.getLastName(),
                convertFromEntity(entity.getShippingInfo()),
                entity.getStatus().toString()
        );
    }
    private CustomerProfileEntity convertToEntity(CustomerProfile profile) {
        return CustomerProfileEntity.builder()
                .userId(profile.getUserId())
                .email(profile.getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .shippingInfo(convertToEntity(profile.getShippingInfo()))
                .build();
    }

    private AddressInfoEntity convertToEntity(AddressInfo addressInfo) {
        return new AddressInfoEntity(
                addressInfo.getFirstName(),
                addressInfo.getLastName(),
                addressInfo.getAddressFirstLine(),
                addressInfo.getAddressSecondLine(),
                addressInfo.getCity(),
                addressInfo.getState(),
                addressInfo.getZipCode()
        );
    }

    private AddressInfo convertFromEntity(AddressInfoEntity entity) {
        return new AddressInfo(
                entity.getFirstName(),
                entity.getLastName(),
                entity.getAddressFirstLine(),
                entity.getAddressSecondLine(),
                entity.getCity(),
                entity.getState(),
                entity.getZipCode()
        );
    }
}
