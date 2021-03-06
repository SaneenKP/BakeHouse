package com.example.temp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.temp.Models.OrderDetails;
import com.example.temp.R;
import com.example.temp.SharedPreferenceConfig;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.narify.netdetect.NetDetect;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

public class RazorpayPayment extends AppCompatActivity implements PaymentResultListener {

    private OrderDetails orderDetails;
    private String dishDetails;
    private SharedPreferenceConfig sharedPreferenceConfig;
    private String noNetworkConnection;
    private RelativeLayout layout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_razorpay_payment);
        Checkout.preload(getApplicationContext());

        NetDetect.init(this);

        sharedPreferenceConfig = new SharedPreferenceConfig(getApplicationContext());
        noNetworkConnection = getApplicationContext().getResources().getString(R.string.noInternet);
        layout = findViewById(R.id.root);

        Intent getOrderDetails = getIntent();
        orderDetails = getOrderDetails.getParcelableExtra("orderDetails");
        dishDetails = getOrderDetails.getStringExtra("dishDetails");

    }

    private void setDishes(String key , DatabaseReference databaseReference)
    {
        try {
            JSONObject orderDishes = new JSONObject(dishDetails);
            for (int i = 0 ; i < orderDishes.names().length() ; i++)
            {
                String Quantity = orderDishes.getString(orderDishes.names().getString(i));
                String dishID = orderDishes.names().getString(i);

                databaseReference.child(key).child(getApplicationContext().getResources().getString(R.string.DishNode)).child(dishID).child("Quantity").setValue(Quantity).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext() , "Failed to set order : "+e , Toast.LENGTH_LONG).show();
                    }
                });

            }

        }catch (Exception e){Toast.makeText(getApplicationContext() , "got and Exception" + e.toString() , Toast.LENGTH_LONG).show();   }

    }

    public void startPayment(String payableAmount , String name) {

        payableAmount = Integer.toString(Integer.parseInt(payableAmount)*100);
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_ZsI3z9yjJt2Y5N");

       checkout.setImage(R.drawable.rzp_logo);

        final Activity activity = this;

        try {

            JSONObject options = new JSONObject();
            options.put("name", name);
            options.put("description", "Reference No. #123456");
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
           // options.put("order_id", "order_DBJOWzybf0sJbb");//from response of step 3.
            options.put("theme.color", "#3399cc");
            options.put("currency", "INR");
            options.put("amount", payableAmount);//pass amount in currency subunits
            options.put("prefill.email", "gaurav.kumar@example.com");
            options.put("prefill.contact", FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() );
            JSONObject retryObj = new JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 4);
            options.put("retry", retryObj);

            checkout.open(activity, options);

        } catch(Exception e) {
            Log.e("Razor Pay Error", "Error in starting Razorpay Checkout", e);
        }
    }

    @Override
    public void onPaymentSuccess(String s) {


        orderDetails.setTransactionId(s);
        DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference().child("Orders");
        String orderKey = firebaseDatabase.push().getKey();
        firebaseDatabase.child(orderKey).setValue(orderDetails);
        setDishes(orderKey , firebaseDatabase);

        Intent orderStatusActivity = new Intent(RazorpayPayment.this , OrderStatus.class);
        sharedPreferenceConfig.writeOrderId(orderKey);
        startActivity(orderStatusActivity);
        finish();
    }

    @Override
    public void onPaymentError(int i, String s) {

            Toast.makeText(getApplicationContext() , "Payment Failed .. " + s , Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        NetDetect.check(isConnected -> {

            if (isConnected) {
                startPayment(orderDetails.getTotal() , orderDetails.getName());
            }else{
                finish();
                Snackbar.make(layout , noNetworkConnection , Snackbar.LENGTH_LONG).show();
            }

        });

    }
}