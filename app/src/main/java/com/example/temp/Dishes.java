package com.example.temp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
}