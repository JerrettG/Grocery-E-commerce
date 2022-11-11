package unit;

import com.gonsalves.CartService.repository.entity.CartItemEntity;
import com.gonsalves.CartService.repository.CartRepository;
import com.gonsalves.CartService.service.Cart.CartItem;
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

    private CartItemEntity cartItemEntity;
    private CartItem cartItem;
    private String id;
    private String userId;
    private String productId;
    private String productName;
    private String productImageUrl;
    private double productPrice;
    @BeforeEach
    protected void setup() {
        id = UUID.randomUUID().toString();
        userId = UUID.randomUUID().toString();
        productId = UUID.randomUUID().toString();
        productName = "Beef Tenderloin";
        productImageUrl = "/demo_images/beefTenderloin.jpg";
        productPrice = 4.99;

        CartItem item = CartItem.builder()
                .id(id)
                .userId(userId)
                .productId(productId)
                .productName(productName)
                .productPrice(productPrice)
                .productImageUrl(productImageUrl).build();
        CartItemEntity entity = new CartItemEntity(
                id,
                userId,
                1,
                productId,
                productName,
                productImageUrl,
                4.99);
        this.cartItemEntity = entity;
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void addItemToCart_itemNotInCart_AddsItemToCart() {

        when(cartRepository.loadCartItem(userId, productId)).thenReturn(new ArrayList<>());
        cartService.addItemToCart(cartItem);

        verify(cartRepository).create(cartItemEntity);
    }
    @Test
    public void addItemToCart_itemAlreadyInCart_itemQuantityIncremented() {

        when(cartRepository.loadCartItem(userId, productId)).thenReturn(Arrays.asList(cartItemEntity));
        cartService.addItemToCart(cartItem);

        verify(cartRepository).updateCartItem(cartItemEntity);
    }
}
