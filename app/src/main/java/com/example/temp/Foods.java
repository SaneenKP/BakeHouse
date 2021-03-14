package com.example.temp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

public class Foods extends AppCompatActivity {

    private GridView foods;
    private String[] foodNames = {"Bread" , "Rice" , "Desert" , "Drinks" , "Shawarma" , "Burger"};
    private int[] foodImages = {R.drawable.bread , R.drawable.rice , R.drawable.dessert , R.drawable.drinks , R.drawable.type6 , R.drawable.type7};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foods);

        foods = findViewById(R.id.foods);

        FoodsViewAdapter foodsViewAdapter = new FoodsViewAdapter(getApplicationContext() , foodNames , foodImages);
        foods.setAdapter(foodsViewAdapter);

        foods.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext() , foodNames[position] , Toast.LENGTH_LONG).show();

            }
        });
    }
}