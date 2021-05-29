package com.example.temp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Hotels extends AppCompatActivity {

    private RecyclerView hotels;
    private RecyclerView.LayoutManager layoutManager;
    private List<HotelDetails> hotelsList;
    private DatabaseReference databaseReference;
    private List<String> hotelKeys;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotels);

        hotels = findViewById(R.id.hotels);
        layoutManager =  new LinearLayoutManager(this);

        hotelsList = new ArrayList<>();
        hotelKeys = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference().child(getApplicationContext().getResources().getString(R.string.HotelNode));

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren())
                {
                    HotelDetails hotelDetails = ds.getValue(HotelDetails.class);
                    hotelsList.add(hotelDetails);
                    hotelKeys.add(ds.getKey());
                }

                HotelViewAdapter hotelViewAdapter = new HotelViewAdapter(getApplicationContext() , hotelsList, hotelKeys);
                hotels.setLayoutManager(layoutManager);
                hotels.setAdapter(hotelViewAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}