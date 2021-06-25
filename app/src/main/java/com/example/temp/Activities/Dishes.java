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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.temp.Adapters.DishesAdapter;
import com.example.temp.Interfaces.EditDishInterface;
import com.example.temp.Models.DishDetails;
import com.example.temp.Models.HotelDetails;
import com.example.temp.R;
import com.example.temp.Interfaces.DishValueInterface;
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
    private int TOTAL_AMOUNT = 0;
    private String hotelKey;
    private JSONObject dishValuesJSON , finalSelectedDishes , finalDishNameAndQuantity;
    private LinearProgressIndicator linearProgressIndicator;
    private SharedPreferenceConfig sharedPreferenceConfig;
    private TextView orderStatus;
    private RelativeLayout layout;
    private HotelDetails hotelDetails;
    private Uri resultUri;
    private AlertDialog dialog;
    private MaterialButton addNewDish;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dishes);
        linearProgressIndicator=findViewById(R.id.dishLoadProgress);
        dishes = findViewById(R.id.dishes);
        totalButton = findViewById(R.id.total_button);
        layoutManager = new LinearLayoutManager(this);
        dishList = new ArrayList<>();

        dishKeyList = new ArrayList<>();

        Bundle b = getIntent().getExtras();
        hotelKey = b.getString("hotel_key");
        hotelDetails = b.getParcelable("hotelDetails");

        hotelDishReference = FirebaseDatabase.getInstance().getReference().child(getApplicationContext().getResources().getString(R.string.HotelNode))
                .child(hotelKey).child(getApplicationContext().getResources().getString(R.string.DishNode));


        sharedPreferenceConfig = new SharedPreferenceConfig(getApplicationContext());

        layout = findViewById(R.id.orderProgressLayout);
        orderStatus = findViewById(R.id.orderStatus);
        layout.setVisibility(View.GONE);

        if (!sharedPreferenceConfig.readOrderId().equals("")){
            showOrderProgress();
        }

        findViewById(R.id.orderStatus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent startOrderStatus = new Intent(Dishes.this , OrderStatus.class);
                startActivity(startOrderStatus);

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
                    openPlaceOrderSection.putExtra("dishNameAndQuantity" , finalDishNameAndQuantity.toString());
                    openPlaceOrderSection.putExtra("hotelKey" , hotelKey);
                    openPlaceOrderSection.putExtra("totalAmount" , TOTAL_AMOUNT);
                    openPlaceOrderSection.putExtra("hotelDetails" , hotelDetails);
                    startActivity(openPlaceOrderSection);
                }

            }
        });

      addNewDish=findViewById(R.id.addNewDish);
      addNewDish.setOnClickListener(new View.OnClickListener() {
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
    protected void onStart() {
        super.onStart();
        totalButton.setText(R.string.no_item_txt);
        TOTAL_AMOUNT=0;
        linearProgressIndicator.setVisibility(View.VISIBLE);
        hotelDishReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()){
                    linearProgressIndicator.setVisibility(View.INVISIBLE);
                    dishList.clear();
                    dishKeyList.clear();

                    for (DataSnapshot dishkeys : task.getResult().getChildren()){
                        String key = dishkeys.getValue(String.class);
                        getDishDetailsReference =  FirebaseDatabase.getInstance().getReference().child(getApplicationContext().getResources().getString(R.string.DishNode)).child(key);
                        getDishDetailsReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {

                                if (task.isSuccessful()){

                                    DishDetails dishDetails = task.getResult().getValue(DishDetails.class);
                                    dishKeyList.add(task.getResult().getKey());
                                    dishList.add(dishDetails);
                                    DishesAdapter dishesAdapter = new DishesAdapter(getApplicationContext(), dishList, dishKeyList, new DishValueInterface() {
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
                                    Toast.makeText(getApplicationContext() , "Sorry .. Data couldn't be retrieved Check Internet Connection",Toast.LENGTH_LONG).show();
                                }

                            }
                        }) ;
                    }

                }

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

    private void addNewDish(DishDetails dishDetails , String key){

        if (resultUri == null && key == null){
            Toast.makeText(getApplicationContext() , "Please Select An Image" , Toast.LENGTH_LONG).show();
        }
        else{

            StorageReference dishImage = FirebaseStorage.getInstance().getReference().child(getApplicationContext().getString(R.string.DishNode)+"/"+dishDetails.getName()+System.currentTimeMillis());
            DatabaseReference dishReference = FirebaseDatabase.getInstance().getReference().child(getApplication().getString(R.string.DishNode));
            dialog.dismiss();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            linearProgressIndicator.setVisibility(View.VISIBLE);
            linearProgressIndicator.setProgress(0);

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

                                                hotelDishReference.child(dishPushKey).setValue(dishPushKey);
                                                resultUri = null;

                                                linearProgressIndicator.setVisibility(View.INVISIBLE);
                                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                Toast.makeText(getApplicationContext(),"Dish Uploaded" , Toast.LENGTH_LONG).show();

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

    private void updateDish(DishDetails dishDetails , String key){

        DatabaseReference dishReference = FirebaseDatabase.getInstance().getReference().child(getApplication().getString(R.string.DishNode)).child(key);

        if (resultUri!=null){

            StorageReference dishImage = FirebaseStorage.getInstance().getReference().child(getApplicationContext().getString(R.string.DishNode)+"/"+dishDetails.getName()+System.currentTimeMillis());
            dialog.dismiss();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            linearProgressIndicator.setVisibility(View.VISIBLE);
            linearProgressIndicator.setProgress(0);


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

            dishReference.setValue(dishDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(getApplicationContext() , "Hotel Value Updated" , Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext() , "Updation failed "+task.getException() , Toast.LENGTH_LONG).show();

                    }
                }
            });


            dialog.dismiss();
        }

    }

    private void deleteDish(String url , String key){

        Log.d("key for deleting" , key);
        StorageReference deleteDishImage = FirebaseStorage.getInstance().getReferenceFromUrl(url);
        deleteDishImage.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                DatabaseReference dishReference = FirebaseDatabase.getInstance().getReference()
                        .child(getApplication().getString(R.string.DishNode));
                dishReference.child(key).removeValue()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {

                            hotelDishReference.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    dialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "Dish Successfully Deleted", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
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

    private void loadingProgressDialog(){


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