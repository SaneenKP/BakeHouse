package com.example.temp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;

public class PlaceOrder extends AppCompatActivity {

    private TextView payableAmount;
    private TextView longitude , lattitude;
    private EditText Add_name , Add_houseNo , Add_locality , Add_landmark , Add_district;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Button pay_and_order , cash_on_delivery;
    private int PERMISSION_ID = 1001;
    private HashMap<String , String> address;
    private SharedPreferenceConfig sharedPreferenceConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        Bundle b = getIntent().getExtras();
        int totalAmount = b.getInt("totalAmount");

        pay_and_order = findViewById(R.id.PAD);
        cash_on_delivery = findViewById(R.id.COD);

        payableAmount = findViewById(R.id.payAmount);
        longitude = findViewById(R.id.longitude);
        lattitude = findViewById(R.id.lattitude);

        Add_name = findViewById(R.id.Add_name);
        Add_houseNo = findViewById(R.id.Add_houseNo);
        Add_locality = findViewById(R.id.Add_locality);
        Add_landmark = findViewById(R.id.Add_landmark);
        Add_district = findViewById(R.id.Add_district);

        address = new HashMap<>();

        sharedPreferenceConfig = new SharedPreferenceConfig(getApplicationContext());
        sharedPreferenceConfig.setDefault();
        if (sharedPreferenceConfig!=null)
        {
            address = sharedPreferenceConfig.readUserAddress();
            Add_name.setText(address.get(getApplicationContext().getResources().getString(R.string.Add_name)));
            Add_houseNo.setText(address.get(getApplicationContext().getResources().getString(R.string.houseNo)));
            Add_locality.setText(address.get(getApplicationContext().getResources().getString(R.string.locality)));
            Add_landmark.setText(address.get(getApplicationContext().getResources().getString(R.string.landmark)));
            Add_district.setText(address.get(getApplicationContext().getResources().getString(R.string.district)));
        }



        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        payableAmount.setText(Integer.toString(totalAmount) + " \u20B9");

        getLastLocation();

        pay_and_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startRazorPay = new Intent(PlaceOrder.this , RazorpayPayment.class);
                startRazorPay.putExtra("payableAmount" ,Integer.toString(totalAmount) );
                startActivity(startRazorPay);
            }
        });

    }

    @SuppressLint("MissingPermission")
    private void getLastLocation()
    {
        if (checkPermissions())
        {
            if (checkLocation())
            {
                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {

                        Location location = task.getResult();
                        if (location == null)
                        {
                            RequestLocationData();
                        }
                        else
                        {
                            lattitude.setText(location.getLatitude()+" ");
                            longitude.setText(location.getLongitude()+" ");

                        }
                    }
                });

            }
            else
            {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }
        else {
            requestPermissions();
        }

    }
    @SuppressLint("MissingPermission")
    private void RequestLocationData()
    {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5);
        locationRequest.setFastestInterval(0);
        locationRequest.setNumUpdates(1);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest , locationCallback , Looper.myLooper());
    }

    private LocationCallback locationCallback = new LocationCallback(){

        @Override
        public void onLocationResult(LocationResult locationResult) {
           Location location = locationResult.getLastLocation();
           lattitude.setText("Latitude = " + location.getLatitude() + " ");
           longitude.setText("Longitutde" + location.getLongitude()+ " ");
        }
    };

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    private boolean checkLocation()
    {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
    }


}