package com.gonsalves.productservice.controller.model;

public interface ProductRequest {

    public String getName();
    public void setName(String name);
    public Double getPrice();
    public void setPrice(Double price);
    public String getUnitMeasurement();
    public void setUnitMeasurement(String unitMeasurement);
    public String getDescription();
    public void setDescription(String description);
    public String getCategory();
    public void setCategory(String category);
    public String getImageUrl();
    public void setImageUrl(String imageUrl);

}
