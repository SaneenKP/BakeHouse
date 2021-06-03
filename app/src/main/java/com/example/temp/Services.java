package com.example.temp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class Services extends AppCompatActivity {

    private GridView services;
    private List<ServiceDetails> servicesName;
    private DatabaseReference getServicesReference;
    private List<String> serviceKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        services = findViewById(R.id.services);
        servicesName = new ArrayList<>();
        serviceKeys = new ArrayList<>();

        getServicesReference = FirebaseDatabase.getInstance().getReference().child(getApplicationContext().getString(R.string.ServicesNode));
        
        getServicesReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                DataSnapshot snapshot = task.getResult();
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    ServiceDetails sd =  dataSnapshot.getValue(ServiceDetails.class);
                    servicesName.add(sd);
                    serviceKeys.add(dataSnapshot.getKey());
                }
                ServicesViewAdapter servicesViewAdapter = new ServicesViewAdapter(getApplicationContext() , servicesName);
                services.setAdapter(servicesViewAdapter);

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