package com.gonsalves.orderservice.unit;

import com.gonsalves.orderservice.repository.entity.AddressInfoEntity;
import com.gonsalves.orderservice.repository.entity.OrderEntity;
import com.gonsalves.orderservice.repository.entity.OrderItemEntity;
import com.gonsalves.orderservice.repository.entity.Status;
import com.gonsalves.orderservice.service.model.AddressInfo;
import com.gonsalves.orderservice.service.model.Order;
import net.andreinc.mockneat.MockNeat;

import java.util.ArrayList;
import java.util.UUID;

public class Util {

    private static final MockNeat mockNeat = MockNeat.threadLocal();
    
    public static Order createOrder() {
        String orderId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        String paymentIntentId = UUID.randomUUID().toString();
        OrderItemEntity orderItemEntity = new OrderItemEntity(
                "Beef Tenderloin",
                "/demo_images/beefTenderloin.jpg",
                2,
                4.99
        );
        AddressInfo shippingAddress = new AddressInfo(
                mockNeat.names().first().valStr(),
                mockNeat.names().last().valStr(),
                mockNeat.addresses().line1().valStr(),
                mockNeat.addresses().line2().valStr(),
                mockNeat.cities().us().valStr(),
                mockNeat.usStates().valStr(),
                mockNeat.ints().range(11111, 99999).valStr()
        );
        AddressInfo billingAddress = new AddressInfo(
                mockNeat.names().first().valStr(),
                mockNeat.names().last().valStr(),
                mockNeat.addresses().line1().valStr(),
                mockNeat.addresses().line2().valStr(),
                mockNeat.cities().us().valStr(),
                mockNeat.usStates().valStr(),
                mockNeat.ints().range(11111, 99999).valStr()
        );

        return new Order(
                orderId,
                userId,
                paymentIntentId,
                shippingAddress,
                billingAddress,
                mockNeat.doubles().val(),
                mockNeat.doubles().val(),
                mockNeat.doubles().val(),
                mockNeat.doubles().val(),
                "PROCESSING",
                new ArrayList<>(),
                mockNeat.localDates().valStr()
        );
    }

    public static OrderEntity createEntity(String userId, String paymentIntentId) {
        String orderId = UUID.randomUUID().toString();
        OrderItemEntity orderItemEntity = new OrderItemEntity(
                "Beef Tenderloin",
                "/demo_images/beefTenderloin.jpg",
                2,
                4.99
        );
        AddressInfoEntity shippingAddress = new AddressInfoEntity(
                mockNeat.names().first().valStr(),
                mockNeat.names().last().valStr(),
                mockNeat.addresses().line1().valStr(),
                mockNeat.addresses().line2().valStr(),
                mockNeat.cities().us().valStr(),
                mockNeat.usStates().valStr(),
                mockNeat.ints().range(11111, 99999).valStr()
        );
        AddressInfoEntity billingAddress = new AddressInfoEntity(
                mockNeat.names().first().valStr(),
                mockNeat.names().last().valStr(),
                mockNeat.addresses().line1().valStr(),
                mockNeat.addresses().line2().valStr(),
                mockNeat.cities().us().valStr(),
                mockNeat.usStates().valStr(),
                mockNeat.ints().range(11111, 99999).valStr()
        );

        return new OrderEntity(
                orderId,
                userId,
                paymentIntentId,
                shippingAddress,
                billingAddress,
                mockNeat.doubles().val(),
                mockNeat.doubles().val(),
                mockNeat.doubles().val(),
                mockNeat.doubles().val(),
                Status.PROCESSING,
                new ArrayList<>(),
                mockNeat.localDates().valStr()
        );
    }

    public static OrderEntity createEntity(Order order) {
        AddressInfoEntity shippingAddress = new AddressInfoEntity(
                order.getShippingInfo().getFirstName(),
                order.getShippingInfo().getLastName(),
                order.getShippingInfo().getAddressFirstLine(),
                order.getShippingInfo().getAddressSecondLine(),
                order.getShippingInfo().getCity(),
                order.getShippingInfo().getState(),
                order.getShippingInfo().getZipCode()
        );
        AddressInfoEntity billingAddress = new AddressInfoEntity(
                order.getBillingInfo().getFirstName(),
                order.getBillingInfo().getLastName(),
                order.getBillingInfo().getAddressFirstLine(),
                order.getBillingInfo().getAddressSecondLine(),
                order.getBillingInfo().getCity(),
                order.getBillingInfo().getState(),
                order.getBillingInfo().getZipCode()
        );

        return new OrderEntity(
                order.getUserId(),
                order.getId(),
                order.getPaymentIntentId(),
                shippingAddress,
                billingAddress,
                mockNeat.doubles().val(),
                mockNeat.doubles().val(),
                mockNeat.doubles().val(),
                mockNeat.doubles().val(),
                Status.PROCESSING,
                new ArrayList<>(),
                mockNeat.localDates().valStr()
        );
    }
    
}
