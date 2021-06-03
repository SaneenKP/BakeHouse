package com.example.temp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Dishes extends AppCompatActivity {

    private RecyclerView dishes;
    private RecyclerView.LayoutManager layoutManager;
    private List<DishDetails> dishList;
    private DatabaseReference databaseReference , getDishDetailsReference;
    private Button totalButton;
    private List<String> dishKeyList;
    private int TOTAL_AMOUNT = 0;
    private String hotelKey;
    private JSONObject dishValuesJSON , finalSelectedDishes;
    private FloatingActionButton fab;
    private AlertDialog dialog;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dishes);

        dishes = findViewById(R.id.dishes);
        layoutManager = new LinearLayoutManager(this);
        dishList = new ArrayList<>();
        dishKeyList = new ArrayList<>();

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

        databaseReference = FirebaseDatabase.getInstance().getReference().child(getApplicationContext().getResources().getString(R.string.HotelNode)).child(hotelKey).child(getApplicationContext().getResources().getString(R.string.DishNode));

        databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {

                if (task.isSuccessful()){

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

                                   DishesAdapter dishesAdapter = new DishesAdapter(getApplicationContext(), dishList, dishKeyList, new dishValuesInterface() {
                                       @Override
                                       public void getCounterValue(int[] value, String[] keys , JSONObject dishValues) {

                                           dishValuesJSON = dishValues;
                                           TOTAL_AMOUNT = 0;

                                           for (int x : value)
                                               TOTAL_AMOUNT += x;
                                           totalButton.setText("Place Order " + Integer.toString(TOTAL_AMOUNT) + " \u20B9");
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
            dishPrice.setText(dishDetails.getPrice());
        }

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


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

                  /*  if(updateStatus)
                        updateHotel(newHotelDetails , key);
                    else
                        addNewHotel(newHotelDetails,key);*/
                }

            }
        });

        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

    }

}