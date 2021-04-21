package com.example.temp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Dishes extends AppCompatActivity {

    private RecyclerView dishes;
    private RecyclerView.LayoutManager layoutManager;
    private List<DishDetails> dishList;
    private DatabaseReference databaseReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dishes);

        dishes = findViewById(R.id.dishes);
        layoutManager = new LinearLayoutManager(this);
        dishList = new ArrayList<>();

        Bundle b = getIntent().getExtras();
        String hotelKey = b.getString("hotel_key");

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Date").child("Food").child(hotelKey).child("Dish");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren()){

                    DishDetails dishDetails = ds.getValue(DishDetails.class);
                    dishList.add(dishDetails);
                    Log.d("DISH DETAILS", dishDetails.getName());
                }

                DishesAdapter dishesAdapter = new DishesAdapter(getApplicationContext() , dishList);
                dishes.setLayoutManager(layoutManager);
                dishes.setAdapter(dishesAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}