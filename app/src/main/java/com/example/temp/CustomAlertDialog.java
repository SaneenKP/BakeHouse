package com.example.temp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

public class CustomAlertDialog {

    private Context context;
    private String message;
    private int layout;
    private boolean SetCancellationOnTouchOutside;


    public CustomAlertDialog(Context context, String message) {
        this.context = context;
        this.message = message;
        this.layout = R.layout.progress_dialog;
        SetCancellationOnTouchOutside = false;

    }

    public AlertDialog getAlertDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(layout , null);
        builder.setView(v);

        TextView textView = v.findViewById(R.id.text_progress_bar);
        textView.setText(message);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(SetCancellationOnTouchOutside);
        return dialog;

    }

    public AlertDialog showAlertDialog(){

        AlertDialog dialog = getAlertDialog();
        dialog.show();
        return dialog;
    }


}
