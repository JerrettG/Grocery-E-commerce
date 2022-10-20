import com.gonsalves.ProductService.entity.Product;
import com.gonsalves.ProductService.exception.ProductAlreadyExistsException;
import com.gonsalves.ProductService.exception.ProductNotFoundException;
import com.gonsalves.ProductService.repository.ProductRepository;
import com.gonsalves.ProductService.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class ProductServiceTest {

    @InjectMocks
    private ProductService productService;
    @Mock
    private ProductRepository productRepository;

    private Product product;

    private String productName;

    @BeforeEach
    public void before() {
        this.productName = "Beef Tenderloin";

        this.product = new Product(
                productName,
                4.99,
                "Test description",
                "Meat, Poultry & Seafood",
                "/demo_images/beefTenderloin.jpg",
                4.7);
        this.product.setProductId(UUID.randomUUID().toString());
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void loadProductWithProductName() {

        when(productRepository.loadProductWithProductName(product.getName())).thenReturn(Arrays.asList(product));
        Product result = productService.loadProductWithProductName("Beef Tenderloin");

        assertEquals(product.getProductId(), result.getProductId(), "Expected result to have matching product id when loading by product name, but did not");
    }

    @Test
    public void loadProductWithProductName_productDoesNotExist_throwsProductNotFoundException() {

        when(productRepository.loadProductWithProductName(productName)).thenReturn(new ArrayList<>());

        assertThrows(ProductNotFoundException.class, ()->productService.loadProductWithProductName(productName),
                "Expected ProductNotFoundException to be thrown when loading non-existent product, but was not.");
    }

    @Test
    public void createProduct() {

        when(productRepository.loadProductWithProductName(productName)).thenReturn(new ArrayList<>());
        productService.createProduct(product);

        verify(productRepository).create(product);
    }

    @Test
    public void createProduct_productAlreadyExists_throwsProductAlreadyExistsException() {

        when(productRepository.loadProductWithProductName(productName)).thenReturn(Arrays.asList(product));

       assertThrows(ProductAlreadyExistsException.class, ()->productService.createProduct(product),
               "Expected ProductAlreadyExistsException to be thrown when creating product with the same name, but was not.");
    }

    @Test
    public void updateProduct() {
        when(productRepository.loadProductWithProductName(productName)).thenReturn(Arrays.asList(product));

        productService.updateProduct(product);

        verify(productRepository).update(product);
    }
    @Test
    public void updateProduct_productDoesNotExist_throwsProductNotFoundException() {
        assertThrows(ProductNotFoundException.class, ()->productService.updateProduct(product),
                "Expected updating producting that does not exist to throw ProductNotFoundException, but did not.");
    }

    @Test
    public void deleteProduct() {

        when(productRepository.loadProductWithProductName(productName)).thenReturn(Arrays.asList(product));
        productService.deleteProduct(productName);

        verify(productRepository).delete(product);
    }

    @Test
    public void deleteProduct_productDoesNotExists_throwsProductNotFoundException() {

        when(productRepository.loadProductWithProductName(productName)).thenReturn(new ArrayList<>());

        assertThrows(ProductNotFoundException.class, ()->productService.deleteProduct(productName),
                "Expected delete of product that does not exist to throw ProductNotFoundException, but did not.");
    }
}
