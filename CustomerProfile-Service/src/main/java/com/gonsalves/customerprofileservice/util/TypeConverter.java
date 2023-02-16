package com.gonsalves.customerprofileservice.util;

import com.gonsalves.customerprofileservice.repository.entity.AddressInfoEntity;
import com.gonsalves.customerprofileservice.repository.entity.CustomerProfileEntity;
import com.gonsalves.customerprofileservice.service.model.AddressInfo;
import com.gonsalves.customerprofileservice.service.model.CustomerProfile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class TypeConverter {
    private static final Gson gson = new GsonBuilder().create();

    public static <T> String toJson(T object) {
        return gson.toJson(object);
    }

    public static CustomerProfile fromJson(String json) {
        return gson.fromJson(json, CustomerProfile.class);
    }

    public static CustomerProfile convertFromEntity(CustomerProfileEntity entity) {
        return new CustomerProfile(
                entity.getUserId(),
                entity.getEmail(),
                entity.getFirstName(),
                entity.getLastName(),
                convertFromEntity(entity.getShippingInfo()),
                entity.getStatus().toString()
        );
    }

    public static AddressInfo convertFromEntity(AddressInfoEntity entity) {
        return new AddressInfo(
                entity.getFirstName(),
                entity.getLastName(),
                entity.getAddressFirstLine(),
                entity.getAddressSecondLine(),
                entity.getCity(),
                entity.getState(),
                entity.getZipCode()
        );
    }

    public static CustomerProfileEntity convertToEntity(CustomerProfile profile) {
        return CustomerProfileEntity.builder()
                .userId(profile.getUserId())
                .email(profile.getEmail())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .shippingInfo(convertToEntity(profile.getShippingInfo()))
                .build();
    }

    public static AddressInfoEntity convertToEntity(AddressInfo addressInfo) {
        return new AddressInfoEntity(
                addressInfo.getFirstName(),
                addressInfo.getLastName(),
                addressInfo.getAddressFirstLine(),
                addressInfo.getAddressSecondLine(),
                addressInfo.getCity(),
                addressInfo.getState(),
                addressInfo.getZipCode()
        );
    }

}
