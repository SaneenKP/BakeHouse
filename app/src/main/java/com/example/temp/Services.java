package com.example.temp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Services extends AppCompatActivity {

    private GridView services;
    private List<String> servicesName;
    private DatabaseReference firebaseRealtimeDatabase;
    private List<String> serviceKeys;
    private int[] servicesImage = {R.drawable.plumbing , R.drawable.electrical , R.drawable.housework , R.drawable.gardner , R.drawable.babysitter , R.drawable.driver};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        services = findViewById(R.id.services);
        servicesName = new ArrayList<>();
        serviceKeys = new ArrayList<>();

        firebaseRealtimeDatabase = FirebaseDatabase.getInstance().getReference().child("Date").child("Services");

        firebaseRealtimeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Log.d("the entire snapshot" , dataSnapshot.toString());
                    servicesName.add(dataSnapshot.child("name").getValue(String.class));
                    serviceKeys.add(dataSnapshot.getKey());
                }
                ServicesViewAdapter servicesViewAdapter = new ServicesViewAdapter(getApplicationContext() , servicesName , servicesImage);
                services.setAdapter(servicesViewAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        services.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent getVendor = new Intent(Services.this , Vendors.class);
                getVendor.putExtra("service-key" , serviceKeys.get(position));
                startActivity(getVendor);
            }
        });
    }

}