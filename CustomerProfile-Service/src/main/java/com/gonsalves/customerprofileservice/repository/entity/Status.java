package com.gonsalves.customerprofileservice.entity;

public enum Status {
    ACTIVE("ACTIVE"),
    DEACTIVATED("DEACTIVATED");

    private String status;

    Status(String status) {
        this.status =status;
    }

    public String getStatus() {
        return status;
    }
}
