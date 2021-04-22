package com.example.temp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class PlaceOrder extends AppCompatActivity {

    private TextView payableAmount;
    private TextView location;
    private TextView Add_houseNo , Add_locality , Add_landmark , Add_district;

    private Button pay_and_order , cash_on_delivery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        Bundle b = getIntent().getExtras();
        int totalAmount = b.getInt("totalAmount");

        pay_and_order = findViewById(R.id.PAD);
        cash_on_delivery = findViewById(R.id.COD);

        payableAmount = findViewById(R.id.payAmount);
        location = findViewById(R.id.location);
        Add_houseNo = findViewById(R.id.Add_houseNo);
        Add_locality = findViewById(R.id.Add_locality);
        Add_landmark = findViewById(R.id.Add_landmark);
        Add_district = findViewById(R.id.Add_district);


        payableAmount.setText(Integer.toString(totalAmount) + " \u20B9");




    }
}