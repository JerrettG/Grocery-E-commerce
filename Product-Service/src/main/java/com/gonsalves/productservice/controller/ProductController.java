package com.gonsalves.productservice.controller;

import com.gonsalves.productservice.controller.model.*;
import com.gonsalves.productservice.exception.ProductAlreadyExistsException;
import com.gonsalves.productservice.exception.ProductNotFoundException;
import com.gonsalves.productservice.service.ProductService;
import com.gonsalves.productservice.service.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("ProductController")
@RequestMapping(value = "/api/v1/productService")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping({"/", "/allProducts"})
    public ResponseEntity<ProductListResponse> getAllProducts() {
        List<Product> results = productService.loadAllProducts();
        ProductListResponse body = new ProductListResponse(results);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    @GetMapping("/{productName}")
    public ResponseEntity<ProductResponse> getProductWithProductName(@PathVariable(name = "productName") String name) {
        try {
            Product product = productService.loadProductWithProductName(name);
            return new ResponseEntity<>(createResponse(product), HttpStatus.OK);
        } catch (ProductNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/product")
    public ResponseEntity<String> createProduct(@RequestBody ProductCreateRequest createRequest) {
        try {
            Product product = createProductFromRequest(createRequest);
            product.setRating(0);
            productService.createProduct(product);
            return new ResponseEntity<>("Resource created successfully", HttpStatus.CREATED);
        } catch (ProductAlreadyExistsException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }
    @PutMapping("/product")
    public ResponseEntity<String> updateProductWithProductName(@RequestBody ProductUpdateRequest updateRequest) {
        try {
            Product product = createProductFromRequest(updateRequest);
            product.setProductId(updateRequest.getProductId());
            product.setRating(updateRequest.getRating());
            productService.updateProduct(product);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/product")
    public ResponseEntity<String> deleteProductWithProductName(@RequestParam(name = "name", required = true)String name) {
        try {
            productService.deleteProduct(name);
            return ResponseEntity.noContent().build();
        } catch (ProductNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    private ProductResponse createResponse(Product product) {
        return new ProductResponse(
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getCategory(),
                product.getImageUrl(),
                product.getRating()
        );
    }
    private Product createProductFromRequest(ProductRequest request) {
        return Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .description(request.getDescription())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .build();
    }


}
