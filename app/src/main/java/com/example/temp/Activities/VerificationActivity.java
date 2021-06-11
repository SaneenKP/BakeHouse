package com.example.temp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.temp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.mukesh.OnOtpCompletionListener;
import com.mukesh.OtpView;

import java.util.concurrent.TimeUnit;

public class VerificationActivity extends AppCompatActivity {
    private OtpView otpView;
    private Button resendOTP;
    private String phoneNumber="";
    private String countryCode="";
    private TextView verifyCounter;
    private CountDownTimer countDownTimer;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks Callbacks;
    private String verificationID;
    private ProgressBar progressBar;
    private ImageButton forwardButton;
    private boolean verified=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        otpView=findViewById(R.id.verification_edittext);
        verifyCounter=findViewById(R.id.timeoutText);
        progressBar=findViewById(R.id.progress_circular);
        forwardButton=findViewById(R.id.forward);
        countryCode=getIntent().getStringExtra("Country");
        phoneNumber=getIntent().getStringExtra("Phone");
        progressBar.setVisibility(View.INVISIBLE);
        resendOTP=findViewById(R.id.resend_otp);
        resendOTP.setVisibility(View.INVISIBLE);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                verified=true;
                otpView.setText(phoneAuthCredential.getSmsCode());
                signInWithCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                Toast.makeText(VerificationActivity.this , e.getMessage() , Toast.LENGTH_LONG).show();
                Log.w("ERRROR" , e.getMessage());
                progressBar.setVisibility(View.INVISIBLE);
                forwardButton.setVisibility(View.VISIBLE);
                forwardButton.setImageDrawable(getDrawable(R.drawable.ic_close_));
                verified=false;
            }

            @Override
            public void onCodeSent(final String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                verificationID = s;
            }
        };

        PhoneAuthProvider.getInstance().verifyPhoneNumber("+"+countryCode + phoneNumber, 60, TimeUnit.SECONDS, VerificationActivity.this, Callbacks);
        countDownTimer = new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                verifyCounter.setText(String.format("Automatic Verification : %d s", millisUntilFinished / 1000));
                forwardButton.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);

            }

            public void onFinish() {
                verifyCounter.setText("Please type your otp manually.");
                otpView.setFocusableInTouchMode(true);
                otpView.setFocusable(true);
                progressBar.setVisibility(View.INVISIBLE);
                forwardButton.setVisibility(View.VISIBLE);
                resendOTP.setVisibility(View.VISIBLE);

            }

        }.start();


        otpView.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
                if(verified==false) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, otp);
                    signInWithCredential(credential);
                }
            }
        });

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(verified==false){
                    finish();
                }
            }
        });

        // TO-DO check Code Quality
        resendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recreate();
            }
        });
    }

    private void signInWithCredential(PhoneAuthCredential phoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    FirebaseDatabase.getInstance().getReference().child("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("number")
                            .setValue(countryCode + phoneNumber);

                    toast("verification successful");
                    startActivity(new Intent(VerificationActivity.this, ChooseOption.class));
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finishAffinity();

                } else if (!task.isSuccessful()) {
                    countDownTimer.cancel();
                    progressBar.setVisibility(View.INVISIBLE);
                    forwardButton.setVisibility(View.VISIBLE);
                    forwardButton.setImageDrawable(getDrawable(R.drawable.ic_close_));
                    verified=false;
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        toast("Verification code is invalid or has expired.");
                    } catch (FirebaseAuthInvalidUserException e) {
                        toast("your account was blocked");
                    } catch (RuntimeException e) {
                        toast("Unknown error , Please check your network and try again later.");
                    } catch (Exception e) {
                        toast("Unknown error , Please check your network and try again later.");
                    }
                } else {
                    toast("Unknown error , Please check your network and try again later.");
                }
            }

            public void toast(String e){
                Toast.makeText(getApplicationContext() , e , Toast.LENGTH_LONG).show();
            }
        });

    }
}