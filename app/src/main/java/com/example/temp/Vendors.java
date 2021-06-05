package com.example.temp;

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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    private DatabaseReference vendorReference;
    private RecyclerView.LayoutManager layoutManager;
    private Uri resultUri;
    private AlertDialog dialog , loadingDialog;
    private CircularProgressIndicator circularProgressIndicator;
    private List<String> vendorKeys;
    private FloatingActionButton fab;
    private CustomAlertDialog customAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendors);

        vendors = findViewById(R.id.vendors);
        layoutManager = new LinearLayoutManager(this);
        vendors.setLayoutManager(layoutManager);

        customAlertDialog = new CustomAlertDialog(Vendors.this);
        circularProgressIndicator = findViewById(R.id.circularProgress);
        fab = findViewById(R.id.addNewVendor);

        Bundle b = getIntent().getExtras();

        vendorDetailsList = new ArrayList<>();
        vendorKeys = new ArrayList<>();

        //getting service key from previous activity to reference into vendors

        serviceKey = b.getString("service-key");

        vendorReference = FirebaseDatabase.getInstance().getReference().child(getApplicationContext().getString(R.string.ServicesNode)).child(serviceKey).child(getApplicationContext().getString(R.string.VendorNode));

        //dialog to show loading on the screen while data is fetched

        loadingDialog = customAlertDialog.getDialog();
        loadingDialog.show();


        getVendorData(vendorReference);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAlertDialog(null , null , false);

            }
        });

    }

    private void getVendorData(DatabaseReference vendorReference){

        vendorReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if (task.isSuccessful()){

                    if (task.getResult().getValue() == null){
                        loadingDialog.dismiss();
                        Toast.makeText(getApplicationContext() , "There Are No Available data .. " , Toast.LENGTH_LONG).show();
                    }else
                    {
                        for (DataSnapshot vendors : task.getResult().getChildren())
                        {

                            VendorDetails vd = vendors.getValue(VendorDetails.class);

                            //collects all the vendor push keys for future reference
                            vendorKeys.add(vendors.getKey());
                            vendorDetailsList.add(vd);
                        }

                        vendorsListAdapter = new VendorsListAdapter(getApplicationContext(), vendorDetailsList, vendorKeys, new vendorsCallListenerInterface() {
                            @Override
                            public void getVendorNumber(String number) {

                                //Interface function implementation to get number from recycler view to intent

                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse("tel:" + number));
                                if (intent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(intent);
                                }
                            }
                        }, new EditVendorInterface() {
                            @Override
                            public void editVendor(VendorDetails vendorDetails, String key) {

                                //Interface function implementation to call alert dialog to edit vendor details
                                showAlertDialog(vendorDetails , key , true);
                            }
                        });

                        loadingDialog.dismiss();
                        vendors.setAdapter(vendorsListAdapter);
                    }

                }else{

                    loadingDialog.dismiss();
                    Toast.makeText(getApplicationContext() , "Failed To Fetch ... : " + task.getException() , Toast.LENGTH_LONG ).show();

                }

            }
        });

    }


    //Method to add a new Vendor
    private void addNewVendor(VendorDetails vendorDetails , String key){

        //to check if the user has added an image or not
        if (resultUri == null && key == null){
            Toast.makeText(getApplicationContext() , "Please Select An Image" , Toast.LENGTH_LONG).show();
        }
        else{

            //Firebase storage reference
            StorageReference vendorImage = FirebaseStorage.getInstance().getReference().child(getApplicationContext().getString(R.string.VendorNode)+"/"+vendorDetails.getName()+System.currentTimeMillis());

            //Firebase realtime database reference
            DatabaseReference vendorReference = FirebaseDatabase.getInstance().getReference().child(getApplicationContext().getString(R.string.ServicesNode)).child(serviceKey).child(getApplicationContext().getString(R.string.VendorNode));

            dialog.dismiss();

            //makes the background untouchable when uploading data
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


            circularProgressIndicator.setVisibility(View.VISIBLE);
            circularProgressIndicator.setProgress(0);

            vendorImage.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()){

                        vendorImage.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){

                                    //sets the new url to vendor details
                                    vendorDetails.setProfile_pic(task.getResult().toString());


                                    vendorReference.push().setValue(vendorDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                resultUri = null;
                                                circularProgressIndicator.setVisibility(View.INVISIBLE);
                                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                Toast.makeText(getApplicationContext(),"Vendor Uploaded" , Toast.LENGTH_LONG).show();

                                            }else{
                                                Toast.makeText(getApplicationContext() , "Network Error" + Objects.requireNonNull(task.getException().getMessage()) , Toast.LENGTH_LONG).show();
                                                circularProgressIndicator.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });

                                }else
                                {
                                    Toast.makeText(getApplicationContext() , "Network Error" + Objects.requireNonNull(task.getException().getMessage()) , Toast.LENGTH_LONG).show();
                                    circularProgressIndicator.setProgress(0);
                                    circularProgressIndicator.setVisibility(View.INVISIBLE);
                                }
                            }
                        });

                    }else{
                        Toast.makeText(getApplicationContext() , "Network Error" + Objects.requireNonNull(task.getException().getMessage()) , Toast.LENGTH_LONG).show();
                        circularProgressIndicator.setProgress(0);
                        circularProgressIndicator.setVisibility(View.INVISIBLE);

                    }

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                    circularProgressIndicator.setVisibility(View.VISIBLE);
                    double progress = (100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                    circularProgressIndicator.setProgressCompat((int)progress,true);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(getApplicationContext() , ""+e.getMessage() , Toast.LENGTH_LONG).show();
                    circularProgressIndicator.setVisibility(View.INVISIBLE);
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

        //checks if user has updated the image
        //else it just updates the data
        if (resultUri!=null){

            StorageReference vendorImage = FirebaseStorage.getInstance().getReference().child(getApplicationContext().getString(R.string.VendorNode)+"/"+vendorDetails.getName()+System.currentTimeMillis());

            dialog.dismiss();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            circularProgressIndicator.setVisibility(View.VISIBLE);
            circularProgressIndicator.setProgress(0);

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

                                                circularProgressIndicator.setVisibility(View.INVISIBLE);
                                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                Toast.makeText(getApplicationContext(),"Vendor Updated" , Toast.LENGTH_LONG).show();

                                            }else{
                                                Toast.makeText(getApplicationContext() , "Network Error" + Objects.requireNonNull(task.getException().getMessage()) , Toast.LENGTH_LONG).show();
                                                circularProgressIndicator.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });

                                }else
                                {
                                    Toast.makeText(getApplicationContext() , "Network Error" + Objects.requireNonNull(task.getException().getMessage()) , Toast.LENGTH_LONG).show();
                                    circularProgressIndicator.setProgress(0);
                                    circularProgressIndicator.setVisibility(View.INVISIBLE);
                                }
                            }
                        });

                    }else{
                        Toast.makeText(getApplicationContext() , "Network Error" + Objects.requireNonNull(task.getException().getMessage()) , Toast.LENGTH_LONG).show();
                        circularProgressIndicator.setProgress(0);
                        circularProgressIndicator.setVisibility(View.INVISIBLE);

                    }

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                    circularProgressIndicator.setVisibility(View.VISIBLE);
                    double progress = (100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                    circularProgressIndicator.setProgressCompat((int)progress,true);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(getApplicationContext() , ""+e.getMessage() , Toast.LENGTH_LONG).show();
                    circularProgressIndicator.setVisibility(View.INVISIBLE);
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
                }
                else{
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