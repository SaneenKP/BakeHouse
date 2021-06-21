package com.example.temp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;

public class CheckNetwork {

    private Context context;
    private ViewGroup group;
    private CustomAlertDialog customAlertDialog;
    private AlertDialog alertDialog;
    private Snackbar snackbar;


    public CheckNetwork(Context context, ViewGroup group) {
        this.context = context;
        this.group = group;
        customAlertDialog = new CustomAlertDialog(context , "Loading");
    }

    public boolean check(){

        boolean status = false;

        if (isNetworkConnected()){

            alertDialog = customAlertDialog.showAlertDialog();
            status = true;
            if (internetIsConnected()){
                alertDialog.dismiss();
                status = true;
            }else{
                alertDialog.dismiss();
                status = false;
                if (group!=null){
                    snackbar.make(group , getNoNetworkConnectionError() , Snackbar.LENGTH_LONG).show();
                }else{
                    Toast.makeText(context, getNoNetworkConnectionError(), Toast.LENGTH_SHORT).show();
                }
            }


        }else{

            status = false;
            if (group!=null){
                snackbar.make(group , getInternetNotSwitchedOnError() , Snackbar.LENGTH_LONG).show();
            }else{
                Toast.makeText(context, getInternetNotSwitchedOnError(), Toast.LENGTH_SHORT).show();
            }
        }

        return status;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private boolean internetIsConnected() {
        try {
            String command = "ping -c 1 google.com";
            return (Runtime.getRuntime().exec(command).waitFor() == 0);
        } catch (Exception e) {
            return false;
        }
    }

    private String getInternetNotSwitchedOnError(){
        return context.getResources().getString(R.string.networkNotSwitchedOn);
    }
    private String getNoNetworkConnectionError(){
        return context.getResources().getString(R.string.noInternet);
    }

}
