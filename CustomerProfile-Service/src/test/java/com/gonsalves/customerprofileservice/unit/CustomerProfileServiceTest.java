package com.gonsalves.customerprofileservice.unit;

import com.gonsalves.customerprofileservice.config.CacheStore;
import com.gonsalves.customerprofileservice.controller.model.CustomerProfileCreateRequest;
import com.gonsalves.customerprofileservice.controller.model.CustomerProfileUpdateRequest;
import com.gonsalves.customerprofileservice.repository.entity.CustomerProfileEntity;
import com.gonsalves.customerprofileservice.exception.CustomerProfileAlreadyExistsException;
import com.gonsalves.customerprofileservice.exception.CustomerProfileNotFoundException;
import com.gonsalves.customerprofileservice.repository.CustomerProfileRepository;
import com.gonsalves.customerprofileservice.repository.entity.Status;
import com.gonsalves.customerprofileservice.service.CustomerProfileService;
import com.gonsalves.customerprofileservice.service.model.CustomerProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class CustomerProfileServiceTest {

    @InjectMocks
    private CustomerProfileService customerProfileService;
    @Mock
    private CustomerProfileRepository customerProfileRepository;
    @Mock
    private CacheStore cache;

    private String userId;
    private CustomerProfileEntity profileEntity;
    private CustomerProfile profile;

    @BeforeEach
    public void before() {
        this.userId = UUID.randomUUID().toString();
        this.profileEntity = new CustomerProfileEntity(
                userId,
                "email@test.com",
                "John",
                "Smith",
                "1234 Main St, Sacramento, CA, 92222",
                Status.ACTIVE,
                "July 7, 2022"
        );
        this.profile = new CustomerProfile(
                userId,
                "email@test.com",
                "John",
                "Smith",
                "1234 Main St, Sacramento, CA, 92222",
                Status.ACTIVE.toString()
        );
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void loadCustomerByUserId() {
        when(customerProfileRepository.loadCustomerByUserId(userId)).thenReturn(profileEntity);

        CustomerProfile result = customerProfileService.loadCustomerByUserId(userId);

        assertEquals(profileEntity.getUserId(), result.getUserId(), "Expected result to have matching id, but did not.");
        assertEquals(profileEntity.getUserId(), result.getUserId(), "Expected result to have matching user id, but did not.");
    }

    @Test
    public void loadCustomerByUserId_customerDoesNotExist_throwsCustomerProfileNotFoundException() {
        when(customerProfileRepository.loadCustomerByUserId(userId)).thenReturn(null);

        assertThrows(CustomerProfileNotFoundException.class, ()->customerProfileService.loadCustomerByUserId(userId)
        ,"Expected loading customer that does not exist to throw CustomerProfileNotFoundException, but did not.");
    }

    @Test
    public void createCustomerProfile() {
        when(customerProfileRepository.loadCustomerByUserId(userId)).thenReturn(null);

        customerProfileService.createCustomerProfile(profile);

        verify(customerProfileRepository).createCustomerProfile(any(CustomerProfileEntity.class));
    }

    @Test
    public void createCustomerProfile_profileAlreadyExists_throwsCustomerProfileAlreadyExistsException() {
        when(customerProfileRepository.loadCustomerByUserId(userId)).thenReturn(profileEntity);

        assertThrows(CustomerProfileAlreadyExistsException.class, ()->customerProfileService.createCustomerProfile(profile),
                "Expected creating a profileEntity for customer that already exists to throw CustomerProfileAlreadyExistsException, but did not.");
    }

    @Test
    public void updateCustomerProfile() {
        when(customerProfileRepository.loadCustomerByUserId(userId)).thenReturn(profileEntity);

        customerProfileService.updateCustomerProfile(profile);

        verify(customerProfileRepository).updateCustomerProfile(any(CustomerProfileEntity.class));
    }

    @Test
    public void updateCustomerProfile_profileDoesNotExist_throwsCustomerProfileNotFoundException() {

        when(customerProfileRepository.loadCustomerByUserId(userId)).thenReturn(null);

        assertThrows(CustomerProfileNotFoundException.class, ()-> customerProfileService.updateCustomerProfile(profile),
                "Expected updating profileEntity that does not exist to throw CustomerProfileNotFoundException, but did not.");
    }

    @Test
    public void deleteCustomerProfile() {

        when(customerProfileRepository.loadCustomerByUserId(userId)).thenReturn(profileEntity);

        customerProfileService.deleteCustomerProfile(userId);

        verify(customerProfileRepository).deleteCustomerProfile(any(CustomerProfileEntity.class));
    }

    @Test
    public void deleteCustomerProfile_profileDoesNotExist_throwsCustomerProfileNotFoundException() {

        when(customerProfileRepository.loadCustomerByUserId(userId)).thenReturn(null);

        assertThrows(CustomerProfileNotFoundException.class, ()-> customerProfileService.deleteCustomerProfile(userId),
                "Expected deleting profileEntity that does not exist to throw CustomerProfileNotFoundException, but did not.");
    }



}
