package com.example.temp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.temp.Adapters.DishesAdapter;
import com.example.temp.Models.DishDetails;
import com.example.temp.Models.HotelDetails;
import com.example.temp.R;
import com.example.temp.Interfaces.dishValuesInterface;
import com.example.temp.SharedPreferenceConfig;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.narify.netdetect.NetDetect;

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
    private JSONObject dishValuesJSON , finalSelectedDishes , finalDishNameAndQuantity;
    private LinearProgressIndicator linearProgressIndicator;
    private SharedPreferenceConfig sharedPreferenceConfig;
    private TextView orderStatus;
    private RelativeLayout layout;
    private HotelDetails hotelDetails;
    private String noNetworkConnection;



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


        NetDetect.init(this);
        noNetworkConnection = getApplicationContext().getResources().getString(R.string.noInternet);



        Bundle b = getIntent().getExtras();
        hotelKey = b.getString("hotel_key");
        hotelDetails = b.getParcelable("hotelDetails");

        databaseReference = FirebaseDatabase.getInstance().getReference().child(getApplicationContext().getResources().getString(R.string.HotelNode))
                .child(hotelKey).child(getApplicationContext().getResources().getString(R.string.DishNode));


        sharedPreferenceConfig = new SharedPreferenceConfig(getApplicationContext());

        layout = findViewById(R.id.orderProgressLayout);
        orderStatus = findViewById(R.id.orderStatus);
        layout.setVisibility(View.GONE);


        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NetDetect.check(isConnected -> {
                    if (isConnected){
                        Intent startOrderStatus = new Intent(Dishes.this , OrderStatus.class);
                        startActivity(startOrderStatus);
                    }else{
                        Snackbar.make(layout , noNetworkConnection , Snackbar.LENGTH_LONG).show();
                    }
                });



            }
        });

        totalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NetDetect.check(isConnected -> {

                    if (isConnected){

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
                    }else{

                        Snackbar.make(layout , noNetworkConnection , Snackbar.LENGTH_LONG).show();

                    }

                });


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
                    orderStatus.setText("Placed");
                }
                if (pickedStatus.equals("yes")){
                    orderStatus.setText("");
                    layout.setVisibility(View.VISIBLE);
                    orderStatus.setText("Picked");
                }
                if (deliveredStatus.equals("yes")){
                    orderStatus.setText("");
                    orderStatus.setText("Delivered");
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

        NetDetect.check(isConnected -> {
            if (isConnected){
                if (!sharedPreferenceConfig.readOrderId().equals("")){
                    showOrderProgress();
                }

                linearProgressIndicator.setVisibility(View.VISIBLE);
                databaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
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

                                            DishesAdapter dishesAdapter = new DishesAdapter(Dishes.this, dishList , dishKeyList, new dishValuesInterface() {
                                                @Override
                                                public void getCounterValue(int[] value, String[] keys , JSONObject dishValues , JSONObject dishNameAndQuantity) {

                                                    finalDishNameAndQuantity = dishNameAndQuantity;
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
                                            Toast.makeText(getApplicationContext() , "Sorry .. Data couldn't be retrieved Check Internet Connection",Toast.LENGTH_LONG).show();
                                        }

                                    }
                                }) ;
                            }

                        }

                    }
                });
            }else{
                Snackbar.make(layout , noNetworkConnection , Snackbar.LENGTH_LONG).show();
            }
        });

    }

}