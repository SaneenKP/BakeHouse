package com.example.temp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
    private FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotels);

        hotels = findViewById(R.id.hotels);
        layoutManager =  new LinearLayoutManager(this);

        hotelsList = new ArrayList<>();
        hotelKeys = new ArrayList<>();

        fab = findViewById(R.id.addNewHotel);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(R.layout.add_hotel_alertdialog);
            }
        });


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

    private void addNewHotel(){

    }

    private void showAlertDialog(int layout){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(layout , null);
        builder.setView(v);
        builder.setMessage("Add New Hotel ");

        EditText hotelName = v.findViewById(R.id.newHotelName);
        EditText hotelAddress = v.findViewById(R.id.newHotelAddress);
        EditText hotelLocation = v.findViewById(R.id.newHotelLocation);
        Button addHotel = v.findViewById(R.id.addNewHotel);

        addHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

    }
}