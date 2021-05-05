 package com.example.temp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

 public class PlaceOrder extends AppCompatActivity {

    private TextView payableAmount;
    private TextView LocationAddress;
    private Double latitude, longitude;
    private EditText Add_name , Add_houseNo , Add_housename, Add_landmark , Add_street;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Button pay_and_order , cash_on_delivery;
    private int PERMISSION_ID = 1001 , totalAmount;
    private HashMap<String , String> address;
    private SharedPreferenceConfig sharedPreferenceConfig;
    private OrderDetails orderDetails;
    private String dishDetails , hotelKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        Bundle b = getIntent().getExtras();
        totalAmount = b.getInt("totalAmount");
        dishDetails = b.getString("dishDetails");
        hotelKey = b.getString("hotelKey");

        LocationAddress = findViewById(R.id.address);

        orderDetails = new OrderDetails();

        pay_and_order = findViewById(R.id.PAD);
        cash_on_delivery = findViewById(R.id.COD);

        payableAmount = findViewById(R.id.payAmount);

        Add_name = findViewById(R.id.Add_name);
        Add_houseNo = findViewById(R.id.Add_houseNo);
        Add_housename = findViewById(R.id.Add_houseName);
        Add_landmark = findViewById(R.id.Add_landmark);
        Add_street = findViewById(R.id.Add_street);

        address = new HashMap<>();

        sharedPreferenceConfig = new SharedPreferenceConfig(getApplicationContext());
        sharedPreferenceConfig.setDefault();
        if (sharedPreferenceConfig!=null)
        {
            address = sharedPreferenceConfig.readUserAddress();
            Add_name.setText(address.get(getApplicationContext().getResources().getString(R.string.Add_name)));
            Add_houseNo.setText(address.get(getApplicationContext().getResources().getString(R.string.houseNo)));
            Add_housename.setText(address.get(getApplicationContext().getResources().getString(R.string.houseName)));
            Add_landmark.setText(address.get(getApplicationContext().getResources().getString(R.string.landmark)));
            Add_street.setText(address.get(getApplicationContext().getResources().getString(R.string.street)));
        }


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        payableAmount.setText(Integer.toString(totalAmount) + " \u20B9");

        getLastLocation();

        pay_and_order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPod();
                setOrderDetails();
                Intent startRazorPay = new Intent(PlaceOrder.this , RazorpayPayment.class);
               startRazorPay.putExtra("orderDetails" , orderDetails);
               startRazorPay.putExtra("dishDetails" , dishDetails);
                startActivity(startRazorPay);
                finish();
            }
        });

        cash_on_delivery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCod();
                setOrderDetails();
                dialogBox();

            }
        });

    }

    private void dialogBox()
    {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are You Sure to place Order using Cash On Delivery ? ");
        builder.setCancelable(false);

        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Orders");
                String key = databaseReference.push().getKey();
                databaseReference.child(key).setValue(orderDetails);
                setDishes(key , databaseReference);

                Intent orderStatus = new Intent(PlaceOrder.this , OrderStatus.class);
                orderStatus.putExtra("orderKey" , key);
                startActivity(orderStatus);
                finish();

            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void setDishes(String key , DatabaseReference databaseReference)
    {
        try {
            JSONObject orderDishes = new JSONObject(dishDetails);
            for (int i = 0 ; i < orderDishes.names().length() ; i++)
            {
                String Quantity = orderDishes.getString(orderDishes.names().getString(i));
                String dishID = orderDishes.names().getString(i);

            databaseReference.child(key).child("Dishes").child(dishID).child("Quantity").setValue(Quantity);

            }

        }catch (Exception e){}

    }

    private void setOrderDetails()
    {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String date = dateFormat.format(Calendar.getInstance().getTime());
        String time = timeFormat.format(Calendar.getInstance().getTime());

        orderDetails.setHotelId(hotelKey);
        orderDetails.setTotal(Integer.toString(totalAmount));
        orderDetails.setDate(date);
        orderDetails.setTime(time);
        orderDetails.setLatitude(latitude+"");
        orderDetails.setLongitude(longitude+"");
        orderDetails.setName(Add_name.getText().toString());
        orderDetails.setHouseNo(Add_houseNo.getText().toString());
        orderDetails.setHouseName(Add_housename.getText().toString());
        orderDetails.setLandmark(Add_landmark.getText().toString());
        orderDetails.setStreet(Add_street.getText().toString());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        orderDetails.setUserId(user.getUid());

    }

    private void setCod()
    {
        orderDetails.setCOD("yes");
        orderDetails.setPOD(("no"));
    }
    private void setPod()
    {
        orderDetails.setPOD("yes");
        orderDetails.setCOD("no");

    }

     private String getAddress(Context context , double lattitude , double longitude){

        String address=  null;
        try {

            Geocoder geocoder = new Geocoder(context , Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lattitude , longitude , 1);
            if (addresses.size() >0)
            {
                Address address1 = addresses.get(0);
                address = address1.getAddressLine(0);
            }

        }catch (IOException ex)
        {
            ex.printStackTrace();
        }

        return address;

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
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            String address = getAddress(getApplicationContext() , latitude, longitude);
                            LocationAddress.setText(address);
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