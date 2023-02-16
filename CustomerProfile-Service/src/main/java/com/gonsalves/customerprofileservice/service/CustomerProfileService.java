package com.gonsalves.customerprofileservice.service;

import com.gonsalves.customerprofileservice.caching.DistributedCache;
import com.gonsalves.customerprofileservice.caching.InMemoryCache;
import com.gonsalves.customerprofileservice.repository.entity.AddressInfoEntity;
import com.gonsalves.customerprofileservice.repository.entity.CustomerProfileEntity;
import com.gonsalves.customerprofileservice.exception.CustomerProfileAlreadyExistsException;
import com.gonsalves.customerprofileservice.exception.CustomerProfileNotFoundException;
import com.gonsalves.customerprofileservice.repository.CustomerProfileRepository;
import com.gonsalves.customerprofileservice.repository.entity.Status;
import com.gonsalves.customerprofileservice.service.model.AddressInfo;
import com.gonsalves.customerprofileservice.service.model.CustomerProfile;
import com.gonsalves.customerprofileservice.util.TypeConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerProfileService {
    private final String PROFILE_KEY = "Profile::%s";
    private final int PROFILE_TTL = 60*60;
    private final CustomerProfileRepository customerProfileRepository;
    private final DistributedCache distributedCache;
    @Autowired
    public CustomerProfileService(CustomerProfileRepository customerProfileRepository, DistributedCache distributedCache) {
        this.customerProfileRepository = customerProfileRepository;
        this.distributedCache = distributedCache;
    }
    public CustomerProfile loadCustomerByUserId(String userId) {
        String key = String.format(PROFILE_KEY, userId);
        Optional<String> cachedProfile = distributedCache.getValue(key);
        if (cachedProfile.isPresent())
            return TypeConverter.fromJson(cachedProfile.get());

      CustomerProfileEntity entity = customerProfileRepository.loadCustomerByUserId(userId)
              .orElseThrow(() -> new CustomerProfileNotFoundException("Customer with specified userId does not exist."));

      CustomerProfile profileFromDataDatabase = TypeConverter.convertFromEntity(entity);

      distributedCache.setValue(userId, PROFILE_TTL,TypeConverter.toJson(profileFromDataDatabase));

      return profileFromDataDatabase;
    }

    public void createCustomerProfile(CustomerProfile profile) {
        try {
            CustomerProfile retrievedProfile = loadCustomerByUserId(profile.getUserId());
            if (retrievedProfile.getStatus().equals(Status.ACTIVE.toString()))
                throw new CustomerProfileAlreadyExistsException("Customer with specified userId already exists.");
            else {
                CustomerProfileEntity entity = TypeConverter.convertToEntity(retrievedProfile);
                entity.setStatus(Status.ACTIVE);
                customerProfileRepository.updateCustomerProfile(entity);
            }
        } catch (CustomerProfileNotFoundException e) {
            CustomerProfileEntity entity = TypeConverter.convertToEntity(profile);
            entity.setStatus(Status.ACTIVE);
            customerProfileRepository.createCustomerProfile(entity);
        }
    }

    public void updateCustomerProfile(CustomerProfile profile) {
        CustomerProfile previousProfile = loadCustomerByUserId(profile.getUserId());
        CustomerProfileEntity updatedProfile = TypeConverter.convertToEntity(profile);

        customerProfileRepository.updateCustomerProfile(updatedProfile);

        String key = String.format(PROFILE_KEY, profile.getUserId());
        distributedCache.invalidate(key);
    }

    /**
     * This method performs a hard delete of the customer's profile information 
     * @param userId The userId associated with the profile being deleted
     */
    public void deleteCustomerProfile(String userId) {
        CustomerProfile profile =  loadCustomerByUserId(userId);
        CustomerProfileEntity entity = CustomerProfileEntity.builder().userId(userId).build();

        customerProfileRepository.deleteCustomerProfile(entity);

        String key = String.format(PROFILE_KEY, profile.getUserId());
        distributedCache.invalidate(key);
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
                .shippingInfo(TypeConverter.convertToEntity(profile.getShippingInfo()))
                .build();
        entity.setStatus(Status.INACTIVE);
        customerProfileRepository.updateCustomerProfile(entity);

        String key = String.format(PROFILE_KEY, profile.getUserId());
        distributedCache.invalidate(key);
    }


}
