package com.example.temp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

public class CustomAlertDialog {

    private androidx.appcompat.app.AlertDialog.Builder builder;
    private androidx.appcompat.app.AlertDialog dialog;
    private Context context;

    public CustomAlertDialog(Context context) {

        this.context = context;
    }

    public AlertDialog getDialog(){

        builder = new androidx.appcompat.app.AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view = inflater.inflate(R.layout.custom_loading_dialog, null);
        builder.setView(view);
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}
