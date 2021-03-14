package com.example.temp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

public class Services extends AppCompatActivity {

    private GridView services;

    private String[] servicesName = {"Plumbing" , "Electrical" , "House Work" , "Gardner" , "Babysitter" , "Driver"};
    private int[] servicesImage = {R.drawable.plumbing , R.drawable.electrical , R.drawable.housework , R.drawable.gardner , R.drawable.babysitter , R.drawable.driver};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        services = findViewById(R.id.services);

        ServicesViewAdapter servicesViewAdapter = new ServicesViewAdapter(getApplicationContext() , servicesName , servicesImage);
        services.setAdapter(servicesViewAdapter);

        services.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext() , servicesName[position] , Toast.LENGTH_LONG).show();


            }
        });
    }
}