package com.example.temp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class Dishes extends AppCompatActivity {

    private RecyclerView dishes;
    private RecyclerView.LayoutManager layoutManager;
    private List<DishDetails> dishList;
    private DatabaseReference databaseReference;
    private Button totalButton;
    private int TOTAL_AMOUNT = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dishes);

        dishes = findViewById(R.id.dishes);
        layoutManager = new LinearLayoutManager(this);
        dishList = new ArrayList<>();

        Bundle b = getIntent().getExtras();
        String hotelKey = b.getString("hotel_key");

        totalButton = findViewById(R.id.total_button);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Date").child("Food").child(hotelKey).child("Dish");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren()){

                    DishDetails dishDetails = ds.getValue(DishDetails.class);
                    dishList.add(dishDetails);
                    Log.d("DISH DETAILS", dishDetails.getName());
                }

                DishesAdapter dishesAdapter = new DishesAdapter(getApplicationContext(), dishList, new totalCounterValueInterface() {
                    @Override
                    public void getCounterValue(int[] value) {

                        TOTAL_AMOUNT = 0;
                        for (int x : value)
                            TOTAL_AMOUNT += x;
                        totalButton.setText("Place Order " + Integer.toString(TOTAL_AMOUNT) + " \u20B9");


                    }
                });
                dishes.setLayoutManager(layoutManager);
                dishes.setAdapter(dishesAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        totalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TOTAL_AMOUNT ==0)
                {
                    Toast.makeText(getApplicationContext() , "No Items Selected" , Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Intent openPlaceOrderSection = new Intent(Dishes.this, PlaceOrder.class);
                    openPlaceOrderSection.putExtra("totalAmount" , TOTAL_AMOUNT);
                    startActivity(openPlaceOrderSection);

                }

            }
        });

    }
}