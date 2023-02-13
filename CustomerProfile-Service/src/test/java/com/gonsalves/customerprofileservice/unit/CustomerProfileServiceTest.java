package com.gonsalves.customerprofileservice.unit;

import com.gonsalves.customerprofileservice.config.CacheStore;
import com.gonsalves.customerprofileservice.controller.model.CustomerProfileCreateRequest;
import com.gonsalves.customerprofileservice.controller.model.CustomerProfileUpdateRequest;
import com.gonsalves.customerprofileservice.repository.entity.AddressInfoEntity;
import com.gonsalves.customerprofileservice.repository.entity.CustomerProfileEntity;
import com.gonsalves.customerprofileservice.exception.CustomerProfileAlreadyExistsException;
import com.gonsalves.customerprofileservice.exception.CustomerProfileNotFoundException;
import com.gonsalves.customerprofileservice.repository.CustomerProfileRepository;
import com.gonsalves.customerprofileservice.repository.entity.Status;
import com.gonsalves.customerprofileservice.service.CustomerProfileService;
import com.gonsalves.customerprofileservice.service.model.AddressInfo;
import com.gonsalves.customerprofileservice.service.model.CustomerProfile;
import net.andreinc.mockneat.MockNeat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
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
    private final MockNeat mockNeat = MockNeat.threadLocal();

    @BeforeEach
    public void before() {
        this.userId = UUID.randomUUID().toString();

        AddressInfo shippingAddress = new AddressInfo(
                mockNeat.names().first().valStr(),
                mockNeat.names().last().valStr(),
                mockNeat.addresses().line1().valStr(),
                mockNeat.addresses().line2().valStr(),
                mockNeat.cities().us().valStr(),
                mockNeat.usStates().valStr(),
                mockNeat.ints().range(11111, 99999).valStr()
        );
        this.profile = new CustomerProfile(
                userId,
                "email@test.com",
                "John",
                "Smith",
                shippingAddress,
                Status.ACTIVE.toString()
        );

        AddressInfoEntity shippingAddressEntity = new AddressInfoEntity(
                profile.getShippingInfo().getFirstName(),
                profile.getShippingInfo().getLastName(),
                profile.getShippingInfo().getAddressFirstLine(),
                profile.getShippingInfo().getAddressSecondLine(),
                profile.getShippingInfo().getCity(),
                profile.getShippingInfo().getState(),
                profile.getShippingInfo().getZipCode()
        );
        
        this.profileEntity = new CustomerProfileEntity(
                userId,
                "email@test.com",
                "John",
                "Smith",
                shippingAddressEntity,
                Status.ACTIVE,
                "July 7, 2022"
        );
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void loadCustomerByUserId() {
        when(customerProfileRepository.loadCustomerByUserId(userId)).thenReturn(Optional.ofNullable(profileEntity));

        CustomerProfile result = customerProfileService.loadCustomerByUserId(userId);

        assertEquals(profileEntity.getUserId(), result.getUserId(), "Expected result to have matching id, but did not.");
        assertEquals(profileEntity.getUserId(), result.getUserId(), "Expected result to have matching user id, but did not.");
    }

    @Test
    public void loadCustomerByUserId_customerDoesNotExist_throwsCustomerProfileNotFoundException() {
        when(customerProfileRepository.loadCustomerByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(CustomerProfileNotFoundException.class, ()->customerProfileService.loadCustomerByUserId(userId)
        ,"Expected loading customer that does not exist to throw CustomerProfileNotFoundException, but did not.");
    }

    @Test
    public void createCustomerProfile() {
        when(customerProfileRepository.loadCustomerByUserId(userId)).thenReturn(Optional.empty());

        customerProfileService.createCustomerProfile(profile);

        verify(customerProfileRepository).createCustomerProfile(any(CustomerProfileEntity.class));
    }

    @Test
    public void createCustomerProfile_profileAlreadyExists_throwsCustomerProfileAlreadyExistsException() {
        when(customerProfileRepository.loadCustomerByUserId(userId)).thenReturn(Optional.ofNullable(profileEntity));

        assertThrows(CustomerProfileAlreadyExistsException.class, ()->customerProfileService.createCustomerProfile(profile),
                "Expected creating a profileEntity for customer that already exists to throw CustomerProfileAlreadyExistsException, but did not.");
    }

    @Test
    public void updateCustomerProfile() {
        when(customerProfileRepository.loadCustomerByUserId(userId)).thenReturn(Optional.ofNullable(profileEntity));

        customerProfileService.updateCustomerProfile(profile);

        verify(customerProfileRepository).updateCustomerProfile(any(CustomerProfileEntity.class));
    }

    @Test
    public void updateCustomerProfile_profileDoesNotExist_throwsCustomerProfileNotFoundException() {

        when(customerProfileRepository.loadCustomerByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(CustomerProfileNotFoundException.class, ()-> customerProfileService.updateCustomerProfile(profile),
                "Expected updating profileEntity that does not exist to throw CustomerProfileNotFoundException, but did not.");
    }

    @Test
    public void deleteCustomerProfile() {

        when(customerProfileRepository.loadCustomerByUserId(userId)).thenReturn(Optional.ofNullable(profileEntity));

        customerProfileService.deleteCustomerProfile(userId);

        verify(customerProfileRepository).deleteCustomerProfile(any(CustomerProfileEntity.class));
    }

    @Test
    public void deleteCustomerProfile_profileDoesNotExist_throwsCustomerProfileNotFoundException() {

        when(customerProfileRepository.loadCustomerByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(CustomerProfileNotFoundException.class, ()-> customerProfileService.deleteCustomerProfile(userId),
                "Expected deleting profileEntity that does not exist to throw CustomerProfileNotFoundException, but did not.");
    }



}
