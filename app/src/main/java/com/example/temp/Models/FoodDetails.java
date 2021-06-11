package com.example.temp.Models;

import android.widget.ImageView;

public class FoodDetails {

    private String FoodName;
    private ImageView FoodImage;

    public String getFoodName() {
        return FoodName;
    }

    public void setFoodName(String foodName) {
        FoodName = foodName;
    }

    public ImageView getFoodImage() {
        return FoodImage;
    }

    public void setFoodImage(ImageView foodImage) {
        FoodImage = foodImage;
    }
}
