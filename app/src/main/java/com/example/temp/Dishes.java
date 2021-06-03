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
import android.service.autofill.Dataset;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Dishes extends AppCompatActivity {

    private RecyclerView dishes;
    private RecyclerView.LayoutManager layoutManager;
    private List<DishDetails> dishList;
    private DatabaseReference hotelDishReference, getDishDetailsReference;
    private Button totalButton;
    private List<String> dishKeyList;
    private HashMap<String , String> hotelDishKey;
    private int TOTAL_AMOUNT = 0;
    private String hotelKey;
    private JSONObject dishValuesJSON , finalSelectedDishes;
    private FloatingActionButton fab;
    private AlertDialog dialog;
    private Uri resultUri;
    private CircularProgressIndicator circularProgressIndicator;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dishes);

        circularProgressIndicator = findViewById(R.id.circularProgress);

        dishes = findViewById(R.id.dishes);
        layoutManager = new LinearLayoutManager(this);
        dishList = new ArrayList<>();
        dishKeyList = new ArrayList<>();
        hotelDishKey = new HashMap<>();

        Bundle b = getIntent().getExtras();
        hotelKey = b.getString("hotel_key");

        totalButton = findViewById(R.id.total_button);
        fab = findViewById(R.id.addNewDish);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    showAlertDialog(null , null , false);
            }
        });

        hotelDishReference = FirebaseDatabase.getInstance().getReference().child(getApplicationContext().getResources().getString(R.string.HotelNode)).child(hotelKey).child(getApplicationContext().getResources().getString(R.string.DishNode));

        hotelDishReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if (task.isSuccessful()){

                    for (DataSnapshot dishkeys : task.getResult().getChildren()){

                        String key = dishkeys.getValue(String.class);
                        hotelDishKey.put(key , dishkeys.getKey());
                        getDishDetailsReference =  FirebaseDatabase.getInstance().getReference().child(getApplicationContext().getResources().getString(R.string.DishNode)).child(key);

                        getDishDetailsReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                           @Override
                           public void onComplete(@NonNull Task<DataSnapshot> task) {

                               if (task.isSuccessful()){

                                       DishDetails dishDetails = task.getResult().getValue(DishDetails.class);
                                       dishKeyList.add(task.getResult().getKey());
                                       dishList.add(dishDetails);

                                   DishesAdapter dishesAdapter = new DishesAdapter(getApplicationContext(), dishList, dishKeyList, new dishValuesInterface() {
                                       @Override
                                       public void getCounterValue(int[] value, String[] keys, JSONObject dishValues) {

                                           dishValuesJSON = dishValues;
                                           TOTAL_AMOUNT = 0;

                                           for (int x : value)
                                               TOTAL_AMOUNT += x;
                                           totalButton.setText("Place Order " + Integer.toString(TOTAL_AMOUNT) + " \u20B9");
                                       }
                                   }, new EditDishInterface() {
                                       @Override
                                       public void editDish(DishDetails dishDetails, String key) {
                                           showAlertDialog(dishDetails , key , true);
                                       }
                                   });

                                   dishes.setLayoutManager(layoutManager);
                                   dishes.setAdapter(dishesAdapter);

                               } else{
                                   Toast.makeText(getApplicationContext() , "Sorry .. Data Couldnt be retrieved Check Internet Connection",Toast.LENGTH_LONG).show();
                               }

                           }
                       }) ;
                    }

                }

            }
        });

        totalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TOTAL_AMOUNT ==0)
                {
                    Toast.makeText(getApplicationContext() , "No Items Selected" , Toast.LENGTH_SHORT).show();
                }
                else
                {
                    finalSelectedDishes = new JSONObject();
                    for (String x : dishKeyList)
                    {
                        try {
                            String quantity = dishValuesJSON.getString(x);
                            if (!quantity.equals("0"))
                            {
                                finalSelectedDishes.put(x , quantity);
                            }
                        }catch (Exception e){}

                    }

                    Intent openPlaceOrderSection = new Intent(Dishes.this, PlaceOrder.class);
                    openPlaceOrderSection.putExtra("dishDetails" , finalSelectedDishes.toString());
                    openPlaceOrderSection.putExtra("hotelKey" , hotelKey);
                    openPlaceOrderSection.putExtra("totalAmount" , TOTAL_AMOUNT);
                    startActivity(openPlaceOrderSection);

                }

            }
        });

    }

    private void addNewDish(DishDetails dishDetails , String key){

        if (resultUri == null && key == null){
            Toast.makeText(getApplicationContext() , "Please Select An Image" , Toast.LENGTH_LONG).show();
        }
        else{

            StorageReference dishImage = FirebaseStorage.getInstance().getReference().child(getApplicationContext().getString(R.string.DishNode)+"/"+dishDetails.getName()+System.currentTimeMillis());
            DatabaseReference dishReference = FirebaseDatabase.getInstance().getReference().child(getApplication().getString(R.string.DishNode));
            dialog.dismiss();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            circularProgressIndicator.setVisibility(View.VISIBLE);
            circularProgressIndicator.setProgress(0);

            dishImage.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()){

                        dishImage.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){

                                    dishDetails.setPic(task.getResult().toString());
                                    String dishPushKey = dishReference.push().getKey();

                                    dishReference.child(dishPushKey).setValue(dishDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                  String hotelDishKey = hotelDishReference.push().getKey();
                                                  hotelDishReference.child(hotelDishKey).setValue(dishPushKey);
                                                  resultUri = null;

                                                circularProgressIndicator.setVisibility(View.INVISIBLE);
                                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                Toast.makeText(getApplicationContext(),"Dish Uploaded" , Toast.LENGTH_LONG).show();

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

    private void updateDish(DishDetails dishDetails , String key){

        DatabaseReference dishReference = FirebaseDatabase.getInstance().getReference().child(getApplication().getString(R.string.DishNode)).child(key);

        if (resultUri!=null){

            StorageReference dishImage = FirebaseStorage.getInstance().getReference().child(getApplicationContext().getString(R.string.DishNode)+"/"+dishDetails.getName()+System.currentTimeMillis());
            dialog.dismiss();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            circularProgressIndicator.setVisibility(View.VISIBLE);
            circularProgressIndicator.setProgress(0);


            dishImage.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if (task.isSuccessful()){

                        dishImage.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){

                                    dishDetails.setPic(task.getResult().toString());

                                    dishReference.setValue(dishDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){

                                                resultUri = null;

                                                circularProgressIndicator.setVisibility(View.INVISIBLE);
                                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                Toast.makeText(getApplicationContext(),"Hotel Uploaded" , Toast.LENGTH_LONG).show();

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

            dishReference.setValue(dishDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(getApplicationContext() , "Hotel Value Updated" , Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext() , "Updation Failed "+task.getException() , Toast.LENGTH_LONG).show();

                    }
                }
            });


            dialog.dismiss();
        }

    }

    private void deleteDish(String url , String key){


        StorageReference deleteDishImage = FirebaseStorage.getInstance().getReferenceFromUrl(url);

        deleteDishImage.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                DatabaseReference dishReference = FirebaseDatabase.getInstance().getReference().child(getApplication().getString(R.string.DishNode));
                dishReference.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        hotelDishReference.child(hotelDishKey.get(key)).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(getApplicationContext() , "Dish Successfully Deleted" , Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext() , "Failed ..." + e.toString(),Toast.LENGTH_LONG).show();
            }
        });


    }



    private void showAlertDialog(DishDetails dishDetails , String key , boolean updateStatus){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.add_dish_alertdialog , null);

        builder.setView(v);
        builder.setMessage(" Add New Dish ");

        EditText dishName = v.findViewById(R.id.newDishName);
        EditText dishPrice = v.findViewById(R.id.newDishPrice);
        Button addDish = v.findViewById(R.id.addNewDish);
        Button addImage = v.findViewById(R.id.addNewDishImage);
        Button delete = v.findViewById(R.id.deleteDish);
        delete.setVisibility(View.INVISIBLE);


        if (updateStatus){

            delete.setVisibility(View.VISIBLE);
            dishName.setText(dishDetails.getName());
            dishPrice.setText(Integer.toString(dishDetails.getPrice()));
        }

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteDish(dishDetails.getPic() , key);

            }
        });

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(Dishes.this);

            }
        });

        addDish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(dishName.getText())){
                    dishName.setError("Please set Dish Name");
                }else if (TextUtils.isEmpty(dishPrice.getText())){
                    dishPrice.setError("Please set Price");
                }else{
                    DishDetails newDishDetails = new DishDetails();
                    newDishDetails.setName(dishName.getText().toString());
                    newDishDetails.setPrice(Integer.parseInt(dishPrice.getText().toString()));

                      if(updateStatus){
                          newDishDetails.setPic(dishDetails.getPic());
                          updateDish(newDishDetails , key);
                      }else{
                          addNewDish(newDishDetails,key);
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