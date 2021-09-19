package com.example.temp;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class SharedPreferenceConfig {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SharedPreferenceConfig(Context context) {
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(context.getResources().getString(R.string.user_address),Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    public void writeUserAddress(HashMap<String ,  String> address){

        editor.putString(context.getResources().getString(R.string.Add_name) , address.get(context.getResources().getString(R.string.Add_name)));
        editor.putString(context.getResources().getString(R.string.houseNo) , address.get(context.getResources().getString(R.string.houseNo)));
        editor.putString(context.getResources().getString(R.string.houseName) , address.get(context.getResources().getString(R.string.houseName)));
        editor.putString(context.getResources().getString(R.string.landmark) , address.get(context.getResources().getString(R.string.landmark)));
        editor.putString(context.getResources().getString(R.string.street) , address.get(context.getResources().getString(R.string.street)));
        editor.commit();

    }

    public void writeOrderId(String orderId){

        editor.putString(context.getResources().getString(R.string.orderId) , orderId);
        editor.commit();

    }

    public void writeTotalAmount(int totalAmount){
        editor.putInt(context.getResources().getString(R.string.totalAmount) , totalAmount);
        editor.commit();
    }

    public String readOrderId(){
        return sharedPreferences.getString(context.getResources().getString(R.string.orderId),"");
    }

    public void removeOrderId(){

        editor.remove(context.getResources().getString(R.string.orderId));
        editor.commit();
    }

    public HashMap<String , String> readUserAddress(){

        HashMap<String , String> address = new HashMap<>();
        address.put(context.getResources().getString(R.string.Add_name) , sharedPreferences.getString(context.getResources().getString(R.string.Add_name) , ""));
        address.put(context.getResources().getString(R.string.houseNo) , sharedPreferences.getString(context.getResources().getString(R.string.houseNo) , ""));
        address.put(context.getResources().getString(R.string.houseName) , sharedPreferences.getString(context.getResources().getString(R.string.houseName) , ""));
        address.put(context.getResources().getString(R.string.landmark) , sharedPreferences.getString(context.getResources().getString(R.string.landmark) , ""));
        address.put(context.getResources().getString(R.string.street) , sharedPreferences.getString(context.getResources().getString(R.string.street) , ""));

        return address;

    }

    public int readTotalAmount(){
        return sharedPreferences.getInt(context.getResources().getString(R.string.totalAmount),0);
    }

    public void clearTotalAmount(){
        editor.remove(context.getResources().getString(R.string.totalAmount));
        editor.commit();
    }

    public void clearPreferences(){
        editor.clear();
        editor.commit();
    }

}
