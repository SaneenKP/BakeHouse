       package com.example.temp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.temp.Adapters.ServicesViewAdapter;
import com.example.temp.Interfaces.EditServiceInterface;
import com.example.temp.Models.ServiceDetails;
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

public class Services extends AppCompatActivity {

    private GridView services;
    private List<ServiceDetails> servicesName;
    private DatabaseReference firebaseRealtimeDatabase;
    private DatabaseReference getServicesReference;
    private List<String> serviceKeys;
    private SharedPreferenceConfig sharedPreferenceConfig;
    private Uri resultUri;
    private TextView orderStatus;
    private RelativeLayout layout;
    private MaterialButton fab;
    private AlertDialog dialog;
    private LinearProgressIndicator linearProgressIndicator;
    private ServicesViewAdapter servicesViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        services = findViewById(R.id.services);
        servicesName = new ArrayList<>();
        serviceKeys = new ArrayList<>();
        linearProgressIndicator=findViewById(R.id.hotelLoadProgress);
        sharedPreferenceConfig = new SharedPreferenceConfig(getApplicationContext());
        layout = findViewById(R.id.orderProgressLayout);
        orderStatus = findViewById(R.id.orderStatus);
        layout.setVisibility(View.GONE);
        getServicesReference = FirebaseDatabase.getInstance().getReference().child(getApplicationContext().getString(R.string.ServicesNode));
        fab=findViewById(R.id.addNewService);
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
        linearProgressIndicator.setVisibility(View.VISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(null , null , false);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        displayData();

    }

    private void displayData(){

        serviceKeys.clear();
        servicesName.clear();
        firebaseRealtimeDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot,  String previousChildName) {

                linearProgressIndicator.setVisibility(View.INVISIBLE);
                ServiceDetails serviceDetails  = snapshot.getValue(ServiceDetails.class);

                Log.d("snappppp" , snapshot.toString());

                servicesName.add(0,serviceDetails);
                serviceKeys.add(0,snapshot.getKey());
                servicesViewAdapter = new ServicesViewAdapter(getApplicationContext(), servicesName, serviceKeys, new EditServiceInterface() {
                    @Override
                    public void editService(ServiceDetails serviceDetails, String key) {
                        showAlertDialog(serviceDetails, key, true);
                    }

                    @Override
                    public void openVendor(int position) {
                        Intent getVendor = new Intent(Services.this, Vendors.class);
                        getVendor.putExtra("service-key", serviceKeys.get(position));
                        startActivity(getVendor);
                    }
                });
                services.setAdapter(servicesViewAdapter);

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

            }
        });

    }

    /*private void displayData() {
        firebaseRealtimeDatabase.get()
                .addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                linearProgressIndicator.setVisibility(View.INVISIBLE);
                if(task.isSuccessful()) {
                    serviceKeys.clear();
                    servicesName.clear();

                    DataSnapshot snapshot = task.getResult();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        ServiceDetails sd = dataSnapshot.getValue(ServiceDetails.class);
                        servicesName.add(sd);
                        serviceKeys.add(dataSnapshot.getKey());
                    }
                    ServicesViewAdapter servicesViewAdapter = new ServicesViewAdapter(getApplicationContext(), servicesName, serviceKeys, new EditServiceInterface() {
                        @Override
                        public void editService(ServiceDetails serviceDetails, String key) {
                            showAlertDialog(serviceDetails, key, true);
                        }

                        @Override
                        public void openVendor(int position) {
                            Intent getVendor = new Intent(Services.this, Vendors.class);
                            getVendor.putExtra("service-key", serviceKeys.get(position));
                            startActivity(getVendor);
                        }
                    });
                    services.setAdapter(servicesViewAdapter);
                }else{
                    Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Failed : " + e , Toast.LENGTH_LONG).show();
            }
        });
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        if (!sharedPreferenceConfig.readOrderId().equals("")){
            showOrderProgress();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CropImage.ActivityResult result = CropImage.getActivityResult(data);

        if (resultCode == RESULT_OK){
            resultUri = Objects.requireNonNull(result).getUri();
        }
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


    private void showAlertDialog(ServiceDetails serviceDetails , String key , boolean updateStatus){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.add_service_alertdialog , null);

        builder.setView(v);
        builder.setMessage(" Add New Service ");

        EditText serviceName = v.findViewById(R.id.newServiceName);
        Button addService = v.findViewById(R.id.addNewService);
        Button addImage = v.findViewById(R.id.addNewServiceImage);
        Button delete = v.findViewById(R.id.deleteService);
        delete.setVisibility(View.INVISIBLE);


        if (updateStatus){

            delete.setVisibility(View.VISIBLE);
            serviceName.setText(serviceDetails.getName());
        }

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteService(serviceDetails.getCover_pic() , key);

            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(Services.this);

            }
        });

        addService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(serviceName.getText())){
                    serviceName.setError("Please set service Name");
                }else{
                    ServiceDetails newServiceDetails = new ServiceDetails();
                    newServiceDetails.setName(serviceName.getText().toString());

                    if(updateStatus){
                        newServiceDetails.setCover_pic(serviceDetails.getCover_pic());
                        updateService(newServiceDetails , key);
                    }else{
                        addNewService(newServiceDetails,key);
                    }

                }

            }
        });

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

    }


    private void addNewService(ServiceDetails serviceDetails , String key){

        if (resultUri == null && key == null){
            Toast.makeText(getApplicationContext() , "Please Select An Image" , Toast.LENGTH_LONG).show();
        }
        else{
            StorageReference serviceImage = FirebaseStorage.getInstance().getReference().child(getApplicationContext().getString(R.string.ServicesNode)+"/"+serviceDetails.getName()+System.currentTimeMillis());
            DatabaseReference serviceReference = FirebaseDatabase.getInstance().getReference().child(getApplication().getString(R.string.ServicesNode));
            dialog.dismiss();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            linearProgressIndicator.setVisibility(View.VISIBLE);
            linearProgressIndicator.setProgress(0);

            serviceImage.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()){
                        serviceImage.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){

                                    serviceDetails.setCover_pic(task.getResult().toString());

                                    serviceReference.push().setValue(serviceDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                resultUri = null;
                                                linearProgressIndicator.setVisibility(View.INVISIBLE);
                                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                Toast.makeText(getApplicationContext(),"Service Uploaded" , Toast.LENGTH_LONG).show();

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



    private void updateService(ServiceDetails serviceDetails , String key){

        if (resultUri!=null){
            getServicesReference.child(key).child(getApplicationContext().getString(R.string.serviceName)).setValue(serviceDetails.getName());
            StorageReference serviceImage = FirebaseStorage.getInstance().getReference().child(getApplicationContext().getString(R.string.ServicesNode)+"/"+serviceDetails.getName()+System.currentTimeMillis());
            dialog.dismiss();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            linearProgressIndicator.setVisibility(View.VISIBLE);
            linearProgressIndicator.setProgress(0);
            serviceImage.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()){

                        serviceImage.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){

                                    serviceDetails.setCover_pic(task.getResult().toString());

                                    getServicesReference.child(key).child(getApplicationContext().getString(R.string.serviceImage)).setValue(serviceDetails.getCover_pic()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                resultUri = null;
                                                linearProgressIndicator.setVisibility(View.INVISIBLE);
                                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                Toast.makeText(getApplicationContext(),"Service Uploaded" , Toast.LENGTH_LONG).show();

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
            getServicesReference.child(key).child(getApplicationContext().getString(R.string.serviceName)).setValue(serviceDetails.getName());
            Toast.makeText(getApplicationContext() , "Service Value Updated" , Toast.LENGTH_LONG).show();
            dialog.dismiss();
        }
    }

    private void deleteService(String url , String key){
        StorageReference deleteServiceImage = FirebaseStorage.getInstance().getReferenceFromUrl(url);
        deleteServiceImage.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                DatabaseReference serviceReference = FirebaseDatabase.getInstance().getReference().child(getApplication().getString(R.string.ServicesNode));
                serviceReference.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        dialog.dismiss();
                        Toast.makeText(getApplicationContext() , "Service Successfully Deleted" , Toast.LENGTH_LONG).show();

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
}