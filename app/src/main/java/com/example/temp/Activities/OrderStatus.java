  package com.example.temp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.temp.Models.OrderDetails;
import com.example.temp.R;
import com.example.temp.SharedPreferenceConfig;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OrderStatus extends AppCompatActivity {

    private TextView orderPlaced , orderCompleted , orderPicked ,orderDelivered;
    private DatabaseReference databaseReference;
    private String orderKey;
    private SharedPreferenceConfig sharedPreferenceConfig;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        orderPlaced = findViewById(R.id.orderPlaced);
        orderCompleted = findViewById(R.id.orderCompleted);
        orderDelivered = findViewById(R.id.orderDelivered);
        orderPicked = findViewById(R.id.orderPicked);

        sharedPreferenceConfig = new SharedPreferenceConfig(getApplicationContext());
        orderKey = sharedPreferenceConfig.readOrderId();
        getOrderStatus();

    }

    private void getOrderStatus()
    {

        databaseReference = FirebaseDatabase.getInstance().getReference().child(getApplicationContext().getResources().getString(R.string.OrderNode)).child(orderKey);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String placedIndex = snapshot.child("placedIndex").getValue(String.class);
                String confirmedIndex = snapshot.child("confirmedIndex").getValue(String.class);
                String pickupIndex = snapshot.child("pickupIndex").getValue(String.class);
                String deliveryIndex = snapshot.child("deliveryIndex").getValue(String.class);

                if (confirmedIndex == "yes"){
                    sharedPreferenceConfig.removeOrderId();
                }

                setStatus(placedIndex , confirmedIndex , pickupIndex , deliveryIndex);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getApplicationContext() , "Failed : "+error , Toast.LENGTH_LONG).show();

            }
        });

    }

    private void setStatus(String placedIndex , String confirmedIndex , String pickupIndex ,String deliveryIndex)
    {
        orderPlaced.setCompoundDrawablesWithIntrinsicBounds(placedIndex.equals("yes") ? R.drawable.check:R.drawable.cancelled, 0, 0, 0);
        orderCompleted.setCompoundDrawablesWithIntrinsicBounds(confirmedIndex.equals("yes") ? R.drawable.check:R.drawable.cancelled, 0, 0, 0);
        orderPicked.setCompoundDrawablesWithIntrinsicBounds(pickupIndex.equals("yes") ? R.drawable.check:R.drawable.cancelled, 0, 0, 0);
        orderDelivered.setCompoundDrawablesWithIntrinsicBounds(deliveryIndex.equals("yes") ? R.drawable.check:R.drawable.cancelled, 0, 0, 0);

    }

}