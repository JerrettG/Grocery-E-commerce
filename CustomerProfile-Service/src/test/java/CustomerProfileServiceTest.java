import com.gonsalves.customerprofileservice.entity.CustomerProfile;
import com.gonsalves.customerprofileservice.exception.CustomerProfileAlreadyExistsException;
import com.gonsalves.customerprofileservice.exception.CustomerProfileNotFoundException;
import com.gonsalves.customerprofileservice.repository.CustomerProfileRepository;
import com.gonsalves.customerprofileservice.service.CustomerProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class CustomerProfileServiceTest {

    @InjectMocks
    private CustomerProfileService customerProfileService;
    @Mock
    private CustomerProfileRepository customerProfileRepository;
    private String profileId;
    private String userId;
    private CustomerProfile profile;

    @BeforeEach
    public void before() {
        this.profileId = UUID.randomUUID().toString();
        this.userId = UUID.randomUUID().toString();
        this.profile = new CustomerProfile(
                profileId,
                userId,
                "email@test.com",
                "John",
                "Smith",
                "1234 Main St, Sacramento, CA, 92222",
                "July 7, 2022"
        );
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void loadCustomerByUserId() {
        when(customerProfileRepository.loadCustomerByUserId(userId)).thenReturn(profile);

        CustomerProfile result = customerProfileService.loadCustomerByUserId(userId);

        assertEquals(profile.getId(), result.getId(), "Expected result to have matching id, but did not.");
        assertEquals(profile.getUserId(), result.getUserId(), "Expected result to have matching user id, but did not.");
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

        verify(customerProfileRepository).createCustomerProfile(profile);
    }

    @Test
    public void createCustomerProfile_profileAlreadyExists_throwsCustomerProfileAlreadyExistsException() {
        when(customerProfileRepository.loadCustomerByUserId(userId)).thenReturn(profile);

        assertThrows(CustomerProfileAlreadyExistsException.class, ()->customerProfileService.createCustomerProfile(profile),
                "Expected creating a profile for customer that already exists to throw CustomerProfileAlreadyExistsException, but did not.");
    }

    @Test
    public void updateCustomerProfile() {
        CustomerProfile updatedProfile = new CustomerProfile(
                profileId,
                userId,
                "john@test.com",
                "John",
                "Smitherson",
                "1234 Main St, Sacramento, CA, 92222",
                "July 7, 2022"
        );

        when(customerProfileRepository.loadCustomerByUserId(userId)).thenReturn(profile);

        customerProfileService.updateCustomerProfile(updatedProfile);

        verify(customerProfileRepository).updateCustomerProfile(updatedProfile);
    }

    @Test
    public void updateCustomerProfile_profileDoesNotExist_throwsCustomerProfileNotFoundException() {
        CustomerProfile updatedProfile = new CustomerProfile(
                profileId,
                userId,
                "john@test.com",
                "John",
                "Smitherson",
                "1234 Main St, Sacramento, CA, 92222",
                "July 7, 2022"
        );

        when(customerProfileRepository.loadCustomerByUserId(userId)).thenReturn(null);

        assertThrows(CustomerProfileNotFoundException.class, ()-> customerProfileService.updateCustomerProfile(updatedProfile),
                "Expected updating profile that does not exist to throw CustomerProfileNotFoundException, but did not.");
    }

    @Test
    public void deleteCustomerProfile() {

        when(customerProfileRepository.loadCustomerByUserId(userId)).thenReturn(profile);

        customerProfileService.deleteCustomerProfile(userId);

        verify(customerProfileRepository).deleteCustomerProfile(profile);
    }

    @Test
    public void deleteCustomerProfile_profileDoesNotExist_throwsCustomerProfileNotFoundException() {

        when(customerProfileRepository.loadCustomerByUserId(userId)).thenReturn(null);

        assertThrows(CustomerProfileNotFoundException.class, ()-> customerProfileService.deleteCustomerProfile(userId),
                "Expected deleting profile that does not exist to throw CustomerProfileNotFoundException, but did not.");
    }



}
