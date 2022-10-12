package com.gonsalves.UI.payment;

import com.gonsalves.CartService.entity.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CartItemConverter implements Converter<String, CartItem> {



    @Autowired
    public CartItemConverter() {

    }

    @Override
    public CartItem convert(String source) {
        CartItem cartItem = new CartItem();
        return null;
    }

}