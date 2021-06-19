package com.example.temp;

import android.content.Context;
import android.net.ConnectivityManager;

public class CheckInternet {

    private Context context;

    public CheckInternet(Context context) {
        this.context = context;
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public boolean internetIsConnected() {
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (Exception e) {
            return false;
        }
    }

    public String getInternetNotSwitchedOnError(){
        return context.getResources().getString(R.string.networkNotSwitchedOn);
    }
    public String getNoNetworkConnectionError(){
        return context.getResources().getString(R.string.noInternet);
    }
}
