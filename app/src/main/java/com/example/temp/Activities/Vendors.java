package com.example.temp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.temp.Adapters.VendorsListAdapter;
import com.example.temp.Interfaces.vendorsCallListenerInterface;
import com.example.temp.Models.VendorDetails;
import com.example.temp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Vendors extends AppCompatActivity {

    private RecyclerView vendors;
    private VendorsListAdapter vendorsListAdapter;
    private List<VendorDetails> vendorDetailsList;
    private String serviceKey;
    private DatabaseReference firebaseRealtimeDatabase;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendors);

        vendors = findViewById(R.id.vendors);
        layoutManager = new LinearLayoutManager(this);
        vendors.setLayoutManager(layoutManager);

        Bundle b = getIntent().getExtras();

        vendorDetailsList = new ArrayList<>();

        serviceKey = b.getString("service-key");

        firebaseRealtimeDatabase = FirebaseDatabase.getInstance().getReference()
                .child(getApplicationContext().getString(R.string.ServicesNode)).
                child(serviceKey)
                .child(getApplicationContext().getString(R.string.VendorNode));

        firebaseRealtimeDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                for (DataSnapshot vendors : task.getResult().getChildren())
                {
                    VendorDetails vd = vendors.getValue(VendorDetails.class);
                    vendorDetailsList.add(vd);
                }
                vendorsListAdapter = new VendorsListAdapter(getApplicationContext(), vendorDetailsList, new vendorsCallListenerInterface() {
                    @Override
                    public void getVendorNumber(String number) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + number));
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
                });
                vendors.setAdapter(vendorsListAdapter);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext() , "Failed : "+e , Toast.LENGTH_LONG).show();
            }
        });



    }
}