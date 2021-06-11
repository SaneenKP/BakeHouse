package com.example.temp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.temp.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.hbb20.CountryCodePicker;

public class PhoneAuth extends AppCompatActivity {

    private EditText authEditText;
    private ExtendedFloatingActionButton authNextButton;
    private CountryCodePicker countryCodePicker;
    private String countryCode;
    private String phoneNumber;
    private RelativeLayout relativeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        


        if (FirebaseAuth.getInstance().getCurrentUser() != null)
        {
            Intent intent = new Intent(PhoneAuth.this , ChooseOption.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_phone_auth);

        

        authEditText =findViewById(R.id.editText_carrierNumber);
        authNextButton=findViewById(R.id.auth_next_button);
        countryCodePicker=findViewById(R.id.ccp);
        relativeLayout=findViewById(R.id.phoneAuth);

    }
    @Override
    protected void onStart() {
        super.onStart();
        authNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countryCode=countryCodePicker.getSelectedCountryCode();
                phoneNumber=authEditText.getText().toString();
                if(!countryCode.isEmpty()){
                    if(!phoneNumber.isEmpty()){
                        Intent verifyIntent=new Intent(PhoneAuth.this, VerificationActivity.class);
                        verifyIntent.putExtra("Phone",phoneNumber);
                        verifyIntent.putExtra("Country",countryCode);
                        startActivity(verifyIntent);

                    }else{
                        Toast.makeText(getApplicationContext() , "Please Enter your phone number" , Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext() , "Please Select your country" , Toast.LENGTH_LONG).show();

                }
            }
        });
    }
}