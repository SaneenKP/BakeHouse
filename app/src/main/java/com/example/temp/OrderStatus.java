package com.example.temp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OrderStatus extends AppCompatActivity {

    private TextView orderPlaced , orderCompleted , orderPicked ,orderDelivered;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        orderCompleted = findViewById(R.id.orderCompleted);
        orderDelivered = findViewById(R.id.orderDelivered);
        orderPicked = findViewById(R.id.orderPicked);
        orderDelivered = findViewById(R.id.orderDelivered);
        
        Intent razorpaydata = this.getIntent();
        String orderKey = razorpaydata.getStringExtra("orderKey");

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Orders").child(orderKey);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String 


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        orderCompleted.setCompoundDrawablesWithIntrinsicBounds(R.drawable.cancelled, 0, 0, 0);

    }

    private void setStatus(String key)
    {

    }
}