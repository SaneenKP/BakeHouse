    package com.example.temp;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

    public class ChooseOption extends AppCompatActivity {

    private MaterialButton food , services;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_option);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        food = findViewById(R.id.btn_food);
        services = findViewById(R.id.btn_services);

        toolbar.setTitle(this.getClass().getSimpleName());

        food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               Intent openFoodSection = new Intent(ChooseOption.this , Foods.class);
               startActivity(openFoodSection);

            }
        });

        services.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openServicesSection = new Intent(ChooseOption.this , Services.class);
                startActivity(openServicesSection);
            }
        });
    }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId())
            {
                case R.id.logout_menu:
                    FirebaseAuth.getInstance().signOut();
                    Intent backToLogin = new Intent(ChooseOption.this , PhoneAuth.class);
                    startActivity(backToLogin);
                    finish();
                    break;

                case R.id.about_menu:
                    break;

                case R.id.settings_menu:
                    break;

            }
            return super.onOptionsItemSelected(item);
        }
    }