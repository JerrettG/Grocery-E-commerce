package com.gonsalves.ProductService.entity;

public enum Category {
    BEVERAGES("Beverages"),
    BAKED_GOODS("Baked goods"),
    BREAKFAST_AND_CEREAL("Breakfast and cereal"),
    CONDIMENTS("Condiments"),
    DAIRY_AND_EGGS("Dairy and eggs"),
    FROZEN_FOODS("Frozen foods"),
    GRAINS_AND_PASTA("Grains and pasta"),
    HEALTH_AND_BEAUTY("Health and beauty"),
    MEAT_AND_SEAFOOD("Meat and seafood"),
    PRODUCE("Produce"),
    SPICES("Spices");

    private final String category;

    private Category(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return this.category;
    }
}
