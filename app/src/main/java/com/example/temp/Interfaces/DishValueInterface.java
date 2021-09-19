package com.example.temp.Interfaces;

import org.json.JSONObject;

public interface DishValueInterface {

    void getCounterValue(int[] value , String[] keys , JSONObject dishValues,JSONObject dishNameAndQUantity);
    void dishIncrementCounter(int price );
    void dishDecrementCounter(int price );

}
