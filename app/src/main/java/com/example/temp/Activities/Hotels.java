package com.example.temp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.temp.Adapters.HotelViewAdapter;
import com.example.temp.Interfaces.EditHotelInterface;
import com.example.temp.Models.HotelDetails;
import com.example.temp.R;
import com.example.temp.SharedPreferenceConfig;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private MaterialButton add;
    private AlertDialog dialog;
    private  Uri resultUri;

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
        hotelViewAdapter = new HotelViewAdapter(Hotels.this, hotelsList, hotelKeys, new EditHotelInterface() {
            @Override
            public void editHotel(HotelDetails hotelDetails, String key) {
                showAlertDialog(hotelDetails , key , true);
            }
        });
        hotels.setAdapter(hotelViewAdapter);
        linearProgressIndicator=findViewById(R.id.hotelLoadProgress);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedPreferenceConfig = new SharedPreferenceConfig(getApplicationContext());
        add=findViewById(R.id.addHotel);
        layout = findViewById(R.id.orderProgressLayout);
        orderStatus = findViewById(R.id.orderStatus);
        layout.setVisibility(View.GONE);

        if (!sharedPreferenceConfig.readOrderId().equals("")){
            showOrderProgress();
        }

        findViewById(R.id.orderStatus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent startOrderStatus = new Intent(Hotels .this , OrderStatus.class);
                startActivity(startOrderStatus);

            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(null , null , false);
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

    public void getHotels(){

        hotelsList.clear();
        hotelKeys.clear();
        linearProgressIndicator.setVisibility(View.VISIBLE);

        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot,  String previousChildName) {
                HotelDetails hotelDetails = snapshot.getValue(HotelDetails.class);
                hotelsList.add(0,hotelDetails);
                hotelKeys.add(0,snapshot.getKey());
                linearProgressIndicator.setVisibility(View.INVISIBLE);
                hotelViewAdapter.notifyItemChanged(0,hotelsList.size());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot,  String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot,  String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                linearProgressIndicator.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext() , "Failed : "+error , Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        getHotels();


    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!sharedPreferenceConfig.readOrderId().equals("")){
            showOrderProgress();
        }

    }
    private void showAlertDialog(HotelDetails hotelDetails , String key , boolean updateStatus){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.add_hotel_alertdialog , null);

        builder.setView(v);
        builder.setMessage(" Add New Hotel ");

        EditText hotelName = v.findViewById(R.id.newHotelName);
        EditText hotelAddress = v.findViewById(R.id.newHotelAddress);
        EditText hotelLocation = v.findViewById(R.id.newHotelLocation);
        Button addHotel = v.findViewById(R.id.addNewHotel);
        Button addImage = v.findViewById(R.id.addNewHotelImage);
        Button delete = v.findViewById(R.id.deleteHotel);
        delete.setVisibility(View.INVISIBLE);


        if (updateStatus){
            delete.setVisibility(View.VISIBLE);
            hotelName.setText(hotelDetails.getHotel_name());
            hotelAddress.setText(hotelDetails.getAddress());
            hotelLocation.setText(hotelDetails.getLocation());

        }

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteHotel(hotelDetails.getImage() , key);
            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(Hotels.this);

            }
        });


        addHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(hotelName.getText())){
                    hotelName.setError("Please set Hotel Name");
                }else if (TextUtils.isEmpty(hotelAddress.getText())){
                    hotelAddress.setError("Please set Address");
                }else if(TextUtils.isEmpty(hotelLocation.getText())){
                    hotelLocation.setError("Please give the location");
                }else{
                    HotelDetails newHotelDetails = new HotelDetails();
                    newHotelDetails.setHotel_name(hotelName.getText().toString());
                    newHotelDetails.setAddress(hotelAddress.getText().toString());
                    newHotelDetails.setLocation(hotelLocation.getText().toString());

                    if(updateStatus)
                        updateHotel(newHotelDetails , key);
                    else
                        addNewHotel(newHotelDetails,key);
                }

            }
        });

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void updateHotel(HotelDetails hotelDetails , String key){

        if (resultUri!=null){
            databaseReference.child(key).child(getApplicationContext().getString(R.string.hotelName)).setValue(hotelDetails.getHotel_name());
            databaseReference.child(key).child(getApplicationContext().getString(R.string.hotelLocation)).setValue(hotelDetails.getLocation());
            databaseReference.child(key).child(getApplicationContext().getString(R.string.hotelAddress)).setValue(hotelDetails.getAddress());
            StorageReference hotelImage = FirebaseStorage.getInstance().getReference().child(getApplicationContext().getString(R.string.HotelNode)+"/"+hotelDetails.getHotel_name()+System.currentTimeMillis());
            dialog.dismiss();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            linearProgressIndicator.setVisibility(View.VISIBLE);
            linearProgressIndicator.setProgress(0);


            hotelImage.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()){

                        hotelImage.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){

                                    hotelDetails.setImage(task.getResult().toString());

                                    databaseReference.child(key).child(getApplicationContext().getString(R.string.hotelImage)).setValue(hotelDetails.getImage()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                resultUri = null;

                                                linearProgressIndicator.setVisibility(View.INVISIBLE);
                                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                Toast.makeText(getApplicationContext(),"Hotel Uploaded" , Toast.LENGTH_LONG).show();

                                            }else{
                                                Toast.makeText(getApplicationContext() , "Network Error" + Objects.requireNonNull(task.getException().getMessage()) , Toast.LENGTH_LONG).show();
                                                linearProgressIndicator.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });

                                }else
                                {
                                    Toast.makeText(getApplicationContext() , "Network Error" + Objects.requireNonNull(task.getException().getMessage()) , Toast.LENGTH_LONG).show();
                                    linearProgressIndicator.setProgress(0);
                                    linearProgressIndicator.setVisibility(View.INVISIBLE);
                                }
                            }
                        });

                    }else{
                        Toast.makeText(getApplicationContext() , "Network Error" + Objects.requireNonNull(task.getException().getMessage()) , Toast.LENGTH_LONG).show();
                        linearProgressIndicator.setProgress(0);
                        linearProgressIndicator.setVisibility(View.INVISIBLE);

                    }

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                    linearProgressIndicator.setVisibility(View.VISIBLE);
                    double progress = (100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                    linearProgressIndicator.setProgressCompat((int)progress,true);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(getApplicationContext() , ""+e.getMessage() , Toast.LENGTH_LONG).show();
                    linearProgressIndicator.setVisibility(View.INVISIBLE);
                }
            });

        }else{

            databaseReference.child(key).child(getApplicationContext().getString(R.string.hotelName)).setValue(hotelDetails.getHotel_name());
            databaseReference.child(key).child(getApplicationContext().getString(R.string.hotelLocation)).setValue(hotelDetails.getLocation());
            databaseReference.child(key).child(getApplicationContext().getString(R.string.hotelAddress)).setValue(hotelDetails.getAddress());
            Toast.makeText(getApplicationContext() , "Hotel Value Updated" , Toast.LENGTH_LONG).show();
            dialog.dismiss();

        }

    }


    private void addNewHotel(HotelDetails hotelDetails , String key){

        if (resultUri==null){
            Toast.makeText(getApplicationContext() , "Please Select An Image" , Toast.LENGTH_LONG).show();
        }
        else{

            StorageReference hotelImage = FirebaseStorage.getInstance().getReference().child(getApplicationContext().getString(R.string.HotelNode)+"/"+hotelDetails.getHotel_name()+System.currentTimeMillis());
            dialog.dismiss();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            linearProgressIndicator.setVisibility(View.VISIBLE);
            linearProgressIndicator.setProgress(0);

            hotelImage.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()){

                        hotelImage.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){

                                    hotelDetails.setImage(task.getResult().toString());

                                    databaseReference.push().setValue(hotelDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                resultUri = null;

                                                linearProgressIndicator.setVisibility(View.INVISIBLE);
                                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                Toast.makeText(getApplicationContext(),"Hotel Uploaded" , Toast.LENGTH_LONG).show();
                                                onStart();
                                            }else{
                                                Toast.makeText(getApplicationContext() , "Network Error" + Objects.requireNonNull(task.getException().getMessage()) , Toast.LENGTH_LONG).show();
                                                linearProgressIndicator.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });

                                }else
                                {
                                    Toast.makeText(getApplicationContext() , "Network Error" + Objects.requireNonNull(task.getException().getMessage()) , Toast.LENGTH_LONG).show();
                                    linearProgressIndicator.setProgress(0);
                                    linearProgressIndicator.setVisibility(View.INVISIBLE);
                                }
                            }
                        });

                    }else{
                        Toast.makeText(getApplicationContext() , "Network Error" + Objects.requireNonNull(task.getException().getMessage()) , Toast.LENGTH_LONG).show();
                        linearProgressIndicator.setProgress(0);
                        linearProgressIndicator.setVisibility(View.INVISIBLE);

                    }

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                    linearProgressIndicator.setVisibility(View.VISIBLE);
                    double progress = (100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                    linearProgressIndicator.setProgressCompat((int)progress,true);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(getApplicationContext() , ""+e.getMessage() , Toast.LENGTH_LONG).show();
                    linearProgressIndicator.setVisibility(View.INVISIBLE);
                }
            });

        }

    }

    private void deleteHotel(String url , String key){

        StorageReference deleteHotelImage = FirebaseStorage.getInstance().getReferenceFromUrl(url);

        deleteHotelImage.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                databaseReference.child(key).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext() , "Successfully Deleted" , Toast.LENGTH_LONG).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(getApplicationContext() , "Failed ..." + e.toString(),Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        CropImage.ActivityResult result = CropImage.getActivityResult(data);

        if (resultCode == RESULT_OK){
            resultUri = Objects.requireNonNull(result).getUri();
        }
    }

}