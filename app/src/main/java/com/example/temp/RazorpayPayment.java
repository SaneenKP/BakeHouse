package com.example.temp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

public class RazorpayPayment extends AppCompatActivity implements PaymentResultListener {

    private Button pay;
    private TextView paymentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_razorpay_payment);
        Checkout.preload(getApplicationContext());

        pay = findViewById(R.id.pay);
        paymentId = findViewById(R.id.paymentId);

        Bundle b = getIntent().getExtras();
        String payableAmount = b.getString("payableAmount");

       startPayment(payableAmount);

    }

    public void startPayment(String payableAmount) {

        payableAmount = Integer.toString(Integer.parseInt(payableAmount)*100);
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_ZsI3z9yjJt2Y5N");

       checkout.setImage(R.drawable.rzp_logo);

        final Activity activity = this;

        try {

            JSONObject options = new JSONObject();
            options.put("name", "food app");
            options.put("description", "Reference No. #123456");
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png");
           // options.put("order_id", "order_DBJOWzybf0sJbb");//from response of step 3.
            options.put("theme.color", "#3399cc");
            options.put("currency", "INR");
            options.put("amount", payableAmount);//pass amount in currency subunits
            options.put("prefill.email", "gaurav.kumar@example.com");
            options.put("prefill.contact","7096469416");
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

        paymentId.setText("Succesfully completed = " + s);


    }

    @Override
    public void onPaymentError(int i, String s) {

        paymentId.setText("Failed  = " + s);

    }
}