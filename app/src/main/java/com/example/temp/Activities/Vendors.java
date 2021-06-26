package com.example.temp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

import com.example.temp.Adapters.VendorsListAdapter;
import com.example.temp.Interfaces.EditVendorInterface;
import com.example.temp.Interfaces.VendorsCallListenerInterface;
import com.example.temp.Models.VendorDetails;
import com.example.temp.R;
import com.example.temp.SharedPreferenceConfig;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
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
    private List<String> vendorKeys=new ArrayList<>();
    private Uri resultUri;
    private MaterialButton fab;
    private AlertDialog dialog;
    private LinearProgressIndicator linearProgressIndicator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendors);
        fab = findViewById(R.id.addNewVendor);
        vendors = findViewById(R.id.vendors);
        layoutManager = new LinearLayoutManager(this);
        vendors.setLayoutManager(layoutManager);
        sharedPreferenceConfig = new SharedPreferenceConfig(getApplicationContext());
        linearProgressIndicator=findViewById(R.id.hotelLoadProgress);
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
                    vendorKeys.add(vendors.getKey());
                }
                vendorsListAdapter = new VendorsListAdapter(getApplicationContext(), vendorDetailsList,vendorKeys, new VendorsCallListenerInterface() {
                    @Override
                    public void getVendorNumber(String number) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + number));
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
                },new EditVendorInterface() {
                    @Override
                    public void editVendor(VendorDetails vendorDetails, String key) {
                        showAlertDialog(vendorDetails , key , true);
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

        if (!sharedPreferenceConfig.readOrderId().equals("")){
            showOrderProgress();
        }

        findViewById(R.id.orderStatus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent startOrderStatus = new Intent(Vendors.this , OrderStatus.class);
                startActivity(startOrderStatus);

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
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

    @Override
    protected void onResume() {
        super.onResume();

        if (!sharedPreferenceConfig.readOrderId().equals("")){
            showOrderProgress();
        }

    }

    //Method to add a new Vendor
    private void addNewVendor(VendorDetails vendorDetails , String key){

        if (resultUri == null){
            Toast.makeText(getApplicationContext() , "Please Select An Image" , Toast.LENGTH_LONG).show();
        }
        else{
            StorageReference vendorImage = FirebaseStorage.getInstance().getReference().child(getApplicationContext().getString(R.string.VendorNode)+"/"+vendorDetails.getName()+System.currentTimeMillis());
            DatabaseReference vendorReference = FirebaseDatabase.getInstance().getReference().child(getApplicationContext().getString(R.string.ServicesNode)).child(serviceKey).child(getApplicationContext().getString(R.string.VendorNode));

            dialog.dismiss();

            //makes the background untouchable when uploading data
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            linearProgressIndicator.setVisibility(View.VISIBLE);
            linearProgressIndicator.setProgress(0);
            vendorImage.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()){
                        vendorImage.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){
                                    vendorDetails.setProfile_pic(task.getResult().toString());
                                    vendorReference.push().setValue(vendorDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                resultUri = null;
                                                linearProgressIndicator.setVisibility(View.INVISIBLE);
                                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                Toast.makeText(getApplicationContext(),"Vendor Uploaded" , Toast.LENGTH_LONG).show();

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

    private void deleteVendor(String url , String key){

        StorageReference deleteVendorImageReference = FirebaseStorage.getInstance().getReferenceFromUrl(url);
        deleteVendorImageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                DatabaseReference vendorReference = FirebaseDatabase.getInstance().getReference().child(getApplication().getString(R.string.ServicesNode)).child(serviceKey).child(getApplicationContext().getString(R.string.VendorNode));
                vendorReference.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        dialog.dismiss();
                        Toast.makeText(getApplicationContext() , "Vendor Successfully Deleted" , Toast.LENGTH_LONG).show();

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

    private void updateVendor(VendorDetails vendorDetails , String key){


        DatabaseReference vendorUpdateReference = FirebaseDatabase.getInstance().getReference().child(getApplicationContext().getString(R.string.ServicesNode)).child(serviceKey).child(getApplicationContext().getString(R.string.VendorNode)).child(key);
         if (resultUri!=null){

            StorageReference vendorImage = FirebaseStorage.getInstance().getReference().child(getApplicationContext().getString(R.string.VendorNode)+"/"+vendorDetails.getName()+System.currentTimeMillis());

            dialog.dismiss();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            linearProgressIndicator.setVisibility(View.VISIBLE);
            linearProgressIndicator.setProgress(0);

            vendorImage.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()){

                        vendorImage.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){

                                    vendorDetails.setProfile_pic(task.getResult().toString());

                                    vendorUpdateReference.setValue(vendorDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                resultUri = null;

                                                linearProgressIndicator.setVisibility(View.INVISIBLE);
                                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                Toast.makeText(getApplicationContext(),"Vendor Updated" , Toast.LENGTH_LONG).show();

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


            vendorUpdateReference.setValue(vendorDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){

                        Toast.makeText(getApplicationContext() , "Vendor Value Updated" , Toast.LENGTH_LONG).show();

                    }else{
                        Toast.makeText(getApplicationContext() , "Failed" , Toast.LENGTH_LONG).show();
                    }
                }
            });

            dialog.dismiss();
        }

    }



    private void showAlertDialog(VendorDetails vendorDetails , String key , boolean updateStatus){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.add_vendor_dialog , null);

        builder.setView(v);

        builder.setMessage(" Add New Vendor ");

        EditText vendorName = v.findViewById(R.id.newVendorName);
        EditText vendorAddress = v.findViewById(R.id.newVendorAddress);
        EditText vendorDescription = v.findViewById(R.id.newVendorDescription);
        EditText vendorPhoneNumber = v.findViewById(R.id.newVendorNumber);
        Button addVendor  = v.findViewById(R.id.addNewVendor);
        Button addImage = v.findViewById(R.id.addNewVendorImage);
        Button delete = v.findViewById(R.id.deleteVendor);
        delete.setVisibility(View.INVISIBLE);


        if (updateStatus){
            delete.setVisibility(View.VISIBLE);
            vendorName.setText(vendorDetails.getName());
            vendorAddress.setText(vendorDetails.getAddress());
            vendorDescription.setText(vendorDetails.getDescription());
            vendorPhoneNumber.setText(vendorDetails.getNumber());
        }

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteVendor(vendorDetails.getProfile_pic() , key);

            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(Vendors.this);

            }
        });

        addVendor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(vendorName.getText())){
                    vendorName.setError("Please set service Name");
                }else if(TextUtils.isEmpty(vendorAddress.getText())){
                    vendorAddress.setError("Please set Address");
                }else if(TextUtils.isEmpty(vendorDescription.getText())){
                    vendorDescription.setError("Please set Description");
                }else if(TextUtils.isEmpty(vendorPhoneNumber.getText())){
                    vendorPhoneNumber.setError("Please set Phone number");
                }else{
                    VendorDetails newVendorDetails = new VendorDetails();
                    newVendorDetails.setName(vendorName.getText().toString());
                    newVendorDetails.setAddress(vendorAddress.getText().toString());
                    newVendorDetails.setDescription(vendorDescription.getText().toString());
                    newVendorDetails.setNumber(vendorPhoneNumber.getText().toString());
                    if(updateStatus){
                        newVendorDetails.setProfile_pic(vendorDetails.getProfile_pic());
                        updateVendor(newVendorDetails , key);
                    }else{
                        addNewVendor(newVendorDetails,key);
                    }

                }

            }
        });

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

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