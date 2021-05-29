package com.example.temp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

public class CustomAlertDialog {

    private Context context;
    private String message;
    private int layout;
    private boolean SetCancellationOnTouchOutside;



    public CustomAlertDialog(Context context, String message, int layout, boolean setCancellationOnTouchOutside) {
        this.context = context;
        this.message = message;
        this.layout = layout;
        SetCancellationOnTouchOutside = setCancellationOnTouchOutside;
    }

    private AlertDialog showAlertDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(layout , null);
        builder.setView(v);
        builder.setMessage(message);

        EditText hotelName = v.findViewById(R.id.newHotelName);
        EditText hotelAddress = v.findViewById(R.id.newHotelAddress);
        EditText hotelLocation = v.findViewById(R.id.newHotelLocation);
        Button addHotel = v.findViewById(R.id.addNewHotel);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(SetCancellationOnTouchOutside);
        return dialog;

    }

}
