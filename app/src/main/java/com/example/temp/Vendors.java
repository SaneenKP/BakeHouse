package com.example.temp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

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

        firebaseRealtimeDatabase = FirebaseDatabase.getInstance().getReference().child("Date").child("Services").child(serviceKey).child("Vendor");

        firebaseRealtimeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot vendors : snapshot.getChildren())
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        

    }
}