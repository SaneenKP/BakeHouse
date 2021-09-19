package com.example.temp.Activities;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import com.example.temp.Models.OrderDetails;
import com.example.temp.R;
import com.example.temp.SharedPreferenceConfig;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class  ChooseOption extends AppCompatActivity {

    private MaterialButton food,services;
    private SharedPreferenceConfig sharedPreferenceConfig;
    private TextView orderStatus;
    private RelativeLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Initializations.
        setContentView(R.layout.activity_choose_option);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        food = findViewById(R.id.btn_food);
        services = findViewById(R.id.btn_services);
        sharedPreferenceConfig = new SharedPreferenceConfig(getApplicationContext());
        layout = findViewById(R.id.orderProgressLayout);
        orderStatus = findViewById(R.id.orderStatus);

        //Order Progress bar set Invisible in the beginning.
        layout.setVisibility(View.GONE);


        //Opens activity Order Status when clicked on the Order Progress bar.
        findViewById(R.id.orderStatus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(ChooseOption .this , OrderStatus.class));

            }
        });

    }

    //Method to control Order progress Bar.
    private void showOrderProgress(){

        //Database Reference to Orders Node and the specific Order ID retrieved from the SharedPreference.

        DatabaseReference orderStatusReference = FirebaseDatabase.getInstance().getReference().child(getApplicationContext().getResources().getString(R.string.OrderNode)).child(sharedPreferenceConfig.readOrderId());

        orderStatusReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String placedStatus =  snapshot.child(getApplicationContext().getResources().getString(R.string.placedIndexStatus)).getValue(String.class);
                String pickedStatus =  snapshot.child(getApplicationContext().getResources().getString(R.string.pickupIndexStatus)).getValue(String.class);
                String deliveredStatus =  snapshot.child(getApplicationContext().getResources().getString(R.string.deliveryIndexStatus)).getValue(String.class);

                // Sets the Progress Bar According to the order Status (yes/no) Retrieved.

                if (placedStatus.equals(getApplicationContext().getResources().getString(R.string.no)) && pickedStatus.equals(getApplicationContext().getResources().getString(R.string.no)) && deliveredStatus.equals(getApplicationContext().getResources().getString(R.string.no))){
                    layout.setVisibility(View.VISIBLE);
                    orderStatus.setText(getApplicationContext().getResources().getString(R.string.orderProgress));
                }
                if (placedStatus.equals(getApplicationContext().getResources().getString(R.string.yes))){
                    orderStatus.setText("");
                    layout.setVisibility(View.VISIBLE);
                    orderStatus.setText(getApplicationContext().getResources().getString(R.string.orderPlacedProgress));
                }
                if (pickedStatus.equals(getApplicationContext().getResources().getString(R.string.yes))){
                    orderStatus.setText("");
                    layout.setVisibility(View.VISIBLE);
                    orderStatus.setText(getApplicationContext().getResources().getString(R.string.orderPickedProgress));
                }

                //Removes the progress bar if the Order Status is "Delivered = yes".
                if (deliveredStatus.equals(getApplicationContext().getResources().getString(R.string.yes))){
                    orderStatus.setText("");
                    orderStatus.setText(getApplicationContext().getResources().getString(R.string.orderDeliveredProgress));
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

        //Order Progressbar displayed whenever the activity resumes.
        if (!sharedPreferenceConfig.readOrderId().equals("")){
            showOrderProgress();
        }

    }


    @Override
    protected void onStart() {
        super.onStart();

        food.setOnClickListener(v -> {
            Intent openHotelSection = new Intent(ChooseOption.this , Hotels.class);
            startActivity(openHotelSection);
        });
        services.setOnClickListener(v -> {
            Intent openServicesSection = new Intent(ChooseOption.this , Services.class);
            startActivity(openServicesSection);
        });
    }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId())
            {
                case R.id.logout_menu:

                    SharedPreferenceConfig sharedPreferenceConfig = new SharedPreferenceConfig(getApplicationContext());
                    sharedPreferenceConfig.clearPreferences();

                    FirebaseAuth.getInstance().signOut();
                    Intent backToLogin = new Intent(ChooseOption.this , PhoneAuth.class);
                    startActivity(backToLogin);
                    finish();
                    break;

                case R.id.about_menu:
                    break;

                case R.id.settings_menu:
                    break;

            }
            return super.onOptionsItemSelected(item);
        }
    }