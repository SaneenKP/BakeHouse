package com.example.temp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Foods extends AppCompatActivity {

    private GridView foods;
    private DatabaseReference firebaseRealtimeDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foods);

        foods = findViewById(R.id.foods);

        firebaseRealtimeDatabase = FirebaseDatabase.getInstance().getReference().child("Date").child("Services");

        firebaseRealtimeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Toast.makeText(getApplicationContext(),"REACHED HERE " , Toast.LENGTH_LONG).show();

                    for (DataSnapshot dataSnapshot : snapshot.getChildren())
                    {
                        for (DataSnapshot ds : dataSnapshot.child("Vendor").getChildren()){
                            String address = ds.child("address").getValue(String.class);
                            String desc = ds.child("description").getValue(String.class);
                            String number = ds.child("number").getValue(Long.class).toString();
                            String profilepic = ds.child("profile pic").getValue(String.class);

                            Log.d("number" , number);
                            Log.d("address" ,  address);
                            Log.d("desc" ,desc);
                            Log.d("profile" , profilepic);
                        }
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

       /* FoodsViewAdapter foodsViewAdapter = new FoodsViewAdapter(getApplicationContext() , foodNames , foodImages);
        foods.setAdapter(foodsViewAdapter);

        foods.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext() , foodNames[position] , Toast.LENGTH_LONG).show();

            }
        });*/
    }

}