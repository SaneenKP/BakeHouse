 package com.example.temp.Activities;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.example.temp.CheckNetwork;
import com.example.temp.CustomAlertDialog;
import com.example.temp.R;
import com.example.temp.SharedPreferenceConfig;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ChooseOption extends AppCompatActivity {

    private MaterialButton food,services;
    private Boolean OrderStatus = true;
    private RelativeLayout root;
    private SharedPreferenceConfig sharedPreferenceConfig;
    private TextView orderStatus;
    private RelativeLayout layout;
    private Snackbar snackbar;
    private AlertDialog dialog;
    private CheckNetwork checkNetwork;
    private CustomAlertDialog customAlertDialog = new CustomAlertDialog(ChooseOption.this , "Loading");;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_choose_option);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        checkNetwork = new CheckNetwork(getApplicationContext());


        food = findViewById(R.id.btn_food);
        services = findViewById(R.id.btn_services);
        sharedPreferenceConfig = new SharedPreferenceConfig(getApplicationContext());
        root = findViewById(R.id.root);

        layout = findViewById(R.id.orderProgressLayout);
        orderStatus = findViewById(R.id.orderStatus);
        layout.setVisibility(View.GONE);



        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent startOrderStatus = new Intent(ChooseOption.this , OrderStatus.class);
                startActivity(startOrderStatus);

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

                dialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                dialog.dismiss();
                Toast.makeText(getApplicationContext() , "Failed : " + error , Toast.LENGTH_LONG).show();

            }
        });

    }



    @Override
    protected void onStart() {
        super.onStart();

        dialog = customAlertDialog.showAlertDialog();

        if (checkNetwork.isNetworkConnected()){

            if (checkNetwork.internetIsConnected()){

                if (!sharedPreferenceConfig.readOrderId().equals("")){
                    showOrderProgress();
                }else{
                    dialog.dismiss(); 
                }
            }else{
                dialog.dismiss();
                snackbar.make(layout , checkNetwork.getNoNetworkConnectionError() , Snackbar.LENGTH_LONG).show();
            }
        }else {
            dialog.dismiss();
            snackbar.make(layout , checkNetwork.getInternetNotSwitchedOnError() , Snackbar.LENGTH_LONG).show();
        }

        food.setOnClickListener(v -> {

            if (checkNetwork.isNetworkConnected()){

                dialog.show();

                if (checkNetwork.internetIsConnected()){

                    dialog.dismiss();
                    Intent openHotelSection = new Intent(ChooseOption.this , Hotels.class);
                    startActivity(openHotelSection);
                }else{

                    dialog.dismiss();
                    snackbar.make(layout , checkNetwork.getNoNetworkConnectionError() , Snackbar.LENGTH_LONG).show();
                }

            }else{

                dialog.dismiss();
                snackbar.make(layout , checkNetwork.getInternetNotSwitchedOnError() , Snackbar.LENGTH_LONG).show();
            }

        });
        services.setOnClickListener(v -> {

            if (checkNetwork.isNetworkConnected()){

                dialog.show();
                if (checkNetwork.internetIsConnected()){

                    dialog.dismiss();
                    Intent openServicesSection = new Intent(ChooseOption.this , Services.class);
                    startActivity(openServicesSection);
                }else{
                    dialog.dismiss();
                    snackbar.make(layout , checkNetwork.getNoNetworkConnectionError()  , Snackbar.LENGTH_LONG).show();
                }
            }else{
                dialog.dismiss();
                snackbar.make(layout , checkNetwork.getInternetNotSwitchedOnError() , Snackbar.LENGTH_LONG).show();
            }

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

                    if (checkNetwork.isNetworkConnected()){

                        dialog.show();
                        if (checkNetwork.isNetworkConnected()){

                            dialog.dismiss();
                            SharedPreferenceConfig sharedPreferenceConfig = new SharedPreferenceConfig(getApplicationContext());
                            sharedPreferenceConfig.clearPreferences();
                            FirebaseAuth.getInstance().signOut();
                            Intent backToLogin = new Intent(ChooseOption.this , PhoneAuth.class);
                            startActivity(backToLogin);
                            finish();

                        }else{
                            dialog.dismiss();
                            snackbar.make(layout , checkNetwork.getNoNetworkConnectionError() , Snackbar.LENGTH_LONG).show();
                        }
                    }else{
                        dialog.dismiss();
                        snackbar.make(layout , checkNetwork.getInternetNotSwitchedOnError() , Snackbar.LENGTH_LONG).show();
                    }
                    break;

                case R.id.about_menu:
                    break;

                case R.id.settings_menu:
                    break;

            }
            return super.onOptionsItemSelected(item);
        }


}