package com.gonsalves.customerprofileservice.controller.model;

public interface CustomerProfileRequest {

    public String getUserId();
    public void setUserId(String userId);
    public String getEmail();
    public void setEmail(String email);
    public String getFirstName();
    public void setFirstName(String firstName);
    public String getLastName();
    public void setLastName(String lastName);
    public String getShippingAddress();
    public void setShippingAddress(String shippingAddress);

}
