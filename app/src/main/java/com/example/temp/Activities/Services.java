package com.example.temp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.temp.Adapters.ServicesViewAdapter;
import com.example.temp.Models.ServiceDetails;
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

import java.util.ArrayList;
import java.util.List;

public class Services extends AppCompatActivity {

    private GridView services;
    private List<ServiceDetails> servicesName;
    private DatabaseReference firebaseRealtimeDatabase;
    private List<String> serviceKeys;
    private SharedPreferenceConfig sharedPreferenceConfig;
    private TextView orderStatus;
    private RelativeLayout layout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        services = findViewById(R.id.services);
        servicesName = new ArrayList<>();
        serviceKeys = new ArrayList<>();

        sharedPreferenceConfig = new SharedPreferenceConfig(getApplicationContext());
        layout = findViewById(R.id.orderProgressLayout);
        orderStatus = findViewById(R.id.orderStatus);
        layout.setVisibility(View.GONE);

        if (!sharedPreferenceConfig.readOrderId().equals("")){
            showOrderProgress();
        }


        findViewById(R.id.orderStatus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent startOrderStatus = new Intent(Services.this , OrderStatus.class);
                startActivity(startOrderStatus);

            }
        });

        firebaseRealtimeDatabase = FirebaseDatabase.getInstance().getReference().child(getApplicationContext().getResources().getString(R.string.ServicesNode));
        findViewById(R.id.hotelLoadProgress).setVisibility(View.VISIBLE);
        firebaseRealtimeDatabase.get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                findViewById(R.id.hotelLoadProgress).setVisibility(View.INVISIBLE);
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
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Failed : " + e , Toast.LENGTH_LONG).show();
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

    private void showOrderProgress(){

        DatabaseReference orderStatusReference = FirebaseDatabase.getInstance().getReference().child(getApplicationContext().getResources().getString(R.string.OrderNode)).child(sharedPreferenceConfig.readOrderId());

        orderStatusReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String placedStatus =  snapshot.child(getApplicationContext().getResources().getString(R.string.placedIndexStatus)).getValue(String.class);
                String pickedStatus =  snapshot.child(getApplicationContext().getResources().getString(R.string.pickupIndexStatus)).getValue(String.class);
                String deliveredStatus =  snapshot.child(getApplicationContext().getResources().getString(R.string.deliveryIndexStatus)).getValue(String.class);

                if (placedStatus.equals("no") && pickedStatus.equals("no") && deliveredStatus.equals("no")){
                    layout.setVisibility(View.VISIBLE);
                    orderStatus.setText("Order Under Progress");
                }
                if (placedStatus.equals("yes")){
                    orderStatus.setText("");
                    layout.setVisibility(View.VISIBLE);
                    orderStatus.setText("Your Order Placed");
                }
                if (pickedStatus.equals("yes")){
                    orderStatus.setText("");
                    layout.setVisibility(View.VISIBLE);
                    orderStatus.setText("Your Order Picked up");
                }
                if (deliveredStatus.equals("yes")){
                    orderStatus.setText("");
                    orderStatus.setText("Your order Delivered");
                    sharedPreferenceConfig.removeOrderId();
                    layout.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!sharedPreferenceConfig.readOrderId().equals("")){
            showOrderProgress();
        }

    }
}