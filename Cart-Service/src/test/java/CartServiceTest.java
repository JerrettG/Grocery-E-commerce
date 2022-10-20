import com.gonsalves.CartService.entity.CartItem;
import com.gonsalves.CartService.repository.CartRepository;
import com.gonsalves.CartService.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Mockito.*;


public class CartServiceTest {

    @InjectMocks
    private CartService cartService;
    @Mock
    private CartRepository cartRepository;

    private CartItem cartItem;
    private String id;
    private String userId;
    private String productId;
    @BeforeEach
    protected void setup() {
        id = UUID.randomUUID().toString();
        userId = UUID.randomUUID().toString();
        productId = UUID.randomUUID().toString();
        CartItem item = new CartItem(
                id,
                userId,
                1,
                productId,
                "Beef Tenderloin",
                "/demo_images/beefTenderloin.jpg",
                4.99);
        this.cartItem = item;
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void addItemToCart_itemNotInCart_AddsItemToCart() {

        when(cartRepository.loadCartItem(userId, productId)).thenReturn(new ArrayList<>());
        cartService.addItemToCart(cartItem);

        verify(cartRepository).create(cartItem);
    }
    @Test
    public void addItemToCart_itemAlreadyInCart_itemQuantityIncremented() {

        when(cartRepository.loadCartItem(userId, productId)).thenReturn(Arrays.asList(cartItem));
        cartService.addItemToCart(cartItem);

        verify(cartRepository).updateCartItem(cartItem);
    }
}
