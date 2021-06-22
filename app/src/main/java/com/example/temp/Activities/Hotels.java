package com.example.temp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.temp.Adapters.HotelViewAdapter;
import com.example.temp.Models.HotelDetails;
import com.example.temp.R;
import com.example.temp.SharedPreferenceConfig;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.narify.netdetect.NetDetect;

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
    private SharedPreferenceConfig sharedPreferenceConfig;
    private TextView orderStatus;
    private RelativeLayout layout;
    private Snackbar snackbar;
    private String noNetworkConnection;



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
        hotelViewAdapter = new HotelViewAdapter(this,hotelsList, hotelKeys, layout);
        hotels.setAdapter(hotelViewAdapter);
        linearProgressIndicator=findViewById(R.id.hotelLoadProgress);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedPreferenceConfig = new SharedPreferenceConfig(getApplicationContext());


        NetDetect.init(this);
        noNetworkConnection = getApplicationContext().getResources().getString(R.string.noInternet);


        layout = findViewById(R.id.orderProgressLayout);
        orderStatus = findViewById(R.id.orderStatus);
        layout.setVisibility(View.GONE);

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NetDetect.check(isConnected -> {
                    if (isConnected){
                        Intent startOrderStatus = new Intent(Hotels.this , OrderStatus.class);
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
                    orderStatus.setText("Placed");
                }
                if (pickedStatus.equals("yes")){
                    orderStatus.setText("");
                    layout.setVisibility(View.VISIBLE);
                    orderStatus.setText("Picked");
                }
                if (deliveredStatus.equals("yes")){
                    orderStatus.setText("");
                    orderStatus.setText("Delivered");
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
               if (!sharedPreferenceConfig.readOrderId().equals(""))
                   showOrderProgress();

               linearProgressIndicator.setVisibility(View.VISIBLE);
               databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                   @Override
                   public void onComplete(@NonNull Task<DataSnapshot> task) {

                       hotelsList.clear();
                       hotelKeys.clear();
                       if (task.isSuccessful()) {
                           for (DataSnapshot ds : task.getResult().getChildren()) {
                               HotelDetails hotelDetails = ds.getValue(HotelDetails.class);
                               hotelsList.add(hotelDetails);
                               hotelKeys.add(ds.getKey());
                           }
                           linearProgressIndicator.setVisibility(View.INVISIBLE);
                           hotelViewAdapter.notifyItemChanged(0, hotelsList.size());
                       }
                   }
               }).addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {

                       linearProgressIndicator.setVisibility(View.INVISIBLE);
                       Toast.makeText(getApplicationContext(), "Failed : " + e, Toast.LENGTH_LONG).show();

                   }
               });

           }else{

               Snackbar.make(layout , noNetworkConnection , Snackbar.LENGTH_LONG).show();
               finish();

           }

       });




    }


}
