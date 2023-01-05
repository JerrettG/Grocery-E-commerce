package com.gonsalves.customerprofileservice.controller.model;

import com.gonsalves.customerprofileservice.service.model.AddressInfo;

public interface CustomerProfileRequest {

    public String getUserId();
    public void setUserId(String userId);
    public String getEmail();
    public void setEmail(String email);
    public String getFirstName();
    public void setFirstName(String firstName);
    public String getLastName();
    public void setLastName(String lastName);
    public AddressInfo getShippingInfo();
    public void setShippingInfo(AddressInfo shippingAddress);

}
