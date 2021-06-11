package com.example.temp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;

import com.example.temp.HotelViewAdapter;
import com.example.temp.Models.HotelDetails;
import com.example.temp.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;
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
    private LinearProgressIndicator linearProgressIndicator;
    private HotelViewAdapter hotelViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotels);
        hotels = findViewById(R.id.hotels);
        layoutManager = new LinearLayoutManager(this);
        hotels.setLayoutManager(layoutManager);
        hotelsList = new ArrayList<>();
        hotelKeys = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference().child(getApplicationContext().getResources().getString(R.string.HotelNode));
        hotelViewAdapter = new HotelViewAdapter(Hotels.this,hotelsList, hotelKeys);
        hotels.setAdapter(hotelViewAdapter);
        linearProgressIndicator=findViewById(R.id.hotelLoadProgress);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        linearProgressIndicator.setVisibility(View.VISIBLE);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hotelsList.clear();
                hotelKeys.clear();
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    HotelDetails hotelDetails = ds.getValue(HotelDetails.class);
                    hotelsList.add(hotelDetails);
                    hotelKeys.add(ds.getKey());
                }
                linearProgressIndicator.setVisibility(View.INVISIBLE);
                hotelViewAdapter.notifyItemChanged(0,hotelsList.size());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}