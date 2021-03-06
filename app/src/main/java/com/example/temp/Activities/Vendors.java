package com.example.temp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.temp.Adapters.VendorsListAdapter;
import com.example.temp.Interfaces.vendorsCallListenerInterface;
import com.example.temp.Models.VendorDetails;
import com.example.temp.R;
import com.example.temp.SharedPreferenceConfig;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.narify.netdetect.NetDetect;

import java.util.ArrayList;
import java.util.List;

public class Vendors extends AppCompatActivity {

    private RecyclerView vendors;
    private VendorsListAdapter vendorsListAdapter;
    private List<VendorDetails> vendorDetailsList;
    private String serviceKey;
    private DatabaseReference firebaseRealtimeDatabase;
    private RecyclerView.LayoutManager layoutManager;
    private SharedPreferenceConfig sharedPreferenceConfig;
    private TextView orderStatus;
    private RelativeLayout layout;
    private String noNetworkConnection;
    private LinearLayout root;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendors);

        vendors = findViewById(R.id.vendors);
        layoutManager = new LinearLayoutManager(this);
        vendors.setLayoutManager(layoutManager);
        sharedPreferenceConfig = new SharedPreferenceConfig(getApplicationContext());


        NetDetect.init(this);
        noNetworkConnection = getApplicationContext().getResources().getString(R.string.noInternet);
        root = findViewById(R.id.root);

        Bundle b = getIntent().getExtras();

        vendorDetailsList = new ArrayList<>();

        serviceKey = b.getString("service-key");

        firebaseRealtimeDatabase = FirebaseDatabase.getInstance().getReference()
                .child(getApplicationContext().getString(R.string.ServicesNode)).
                child(serviceKey)
                .child(getApplicationContext().getString(R.string.VendorNode));
        findViewById(R.id.hotelLoadProgress).setVisibility(View.VISIBLE);
        firebaseRealtimeDatabase.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                findViewById(R.id.hotelLoadProgress).setVisibility(View.INVISIBLE);
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

        layout = findViewById(R.id.orderProgressLayout);
        orderStatus = findViewById(R.id.orderStatus);
        layout.setVisibility(View.GONE);

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sharedPreferenceConfig.readOrderId().equals("")){
                    showOrderProgress();
                }
            }
        });

        findViewById(R.id.orderStatus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NetDetect.check(isConnected -> {
                    if (isConnected){
                        Intent startOrderStatus = new Intent(Vendors.this , OrderStatus.class);
                        startActivity(startOrderStatus);
                    }else{
                        Snackbar.make(layout , noNetworkConnection , Snackbar.LENGTH_LONG).show();
                    }
                });
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
    protected void onStart() {
        super.onStart();

        NetDetect.check(isConnected -> {

            if (isConnected){
                if (!sharedPreferenceConfig.readOrderId().equals("")){
                    showOrderProgress();
                }
            }else{
                Snackbar.make(root , noNetworkConnection , Snackbar.LENGTH_LONG).show();
            }


        });
    }
}