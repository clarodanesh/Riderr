package com.riderrapp.riderr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.dropin.utils.PaymentMethodType;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class PaymentActivity extends AppCompatActivity {

    public static final String LONGITUDE = "lng";
    public static final String LATITUDE = "lat";
    public static final String RATING = "rating";
    public static final String AMOUNT_OF_RATINGS = "amt_of_ratings";
    public static final String RIDE_ID = "rideid";
    public static final String PRICE = "price";


    private static final String TAG = "FoundRidesActivity";

    private Map<String, Object> uData = new HashMap<>();

    private FirebaseAuth mAuth;

    private String cT;
    final int REQUEST_CODE = 1;
    AsyncHttpClient client = new AsyncHttpClient();

    String price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        client.get("https://riderr-test.herokuapp.com/checkouts/new", new TextHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, String clientToken) {
                cT = clientToken;
                //do this as the client token is returned as a string already then double quoted
                cT = cT.replace("\"","");
                System.out.println(cT);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {

            }
        });

        price = (String)getIntent().getExtras().get(PRICE);
    }

    public void onBraintreeSubmit(View v) {
        DropInRequest dropInRequest = new DropInRequest()
                .clientToken(cT);
        //these two lines help take out gpay and paypal buttons
        dropInRequest.disableGooglePayment();
        dropInRequest.disablePayPal();
        //dropInRequest.amount("25.00");
        startActivityForResult(dropInRequest.getIntent(this), REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                // use the result to update your UI and send the payment method nonce to your server
                PaymentMethodNonce paymentMethodNonce = result.getPaymentMethodNonce();

                PaymentMethodType paymentMethodType = result.getPaymentMethodType();

                String nonce = (paymentMethodNonce != null ? paymentMethodNonce.getNonce() : null);

                //AddUserToRide();
                postNonceToServer(nonce);
            } else if (resultCode == RESULT_CANCELED) {
                // the user canceled
            } else {
                // handle errors here, an exception may be available in
                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
            }
        }
    }

    public void AddUserToRide(){
        String rideid = (String)getIntent().getExtras().get(RIDE_ID);
        String lng = (String)getIntent().getExtras().get(LONGITUDE);
        String lat = (String)getIntent().getExtras().get(LATITUDE);
        long rating = (long)getIntent().getExtras().get(RATING);
        long amtOfRatings = (long)getIntent().getExtras().get(AMOUNT_OF_RATINGS);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = fbuser.getUid();

        uData.put("passenger", fbuser.getUid());
        uData.put("longitude", lng);
        uData.put("latitude", lat);
        uData.put("rating", rating);
        uData.put("amountOfRatings", amtOfRatings);

        //TODO ADD THE USER DATA TO THE RIDE HERE AND DECREMENT RIDE VCAP
        DocumentReference selectedRideRef = db.collection("OfferedRides").document(rideid);
        selectedRideRef.update("passengers", FieldValue.arrayUnion(uData));
        selectedRideRef.update("vehicleCapacity", FieldValue.increment(-1));

        Map<String, Object> user = new HashMap<>();
        user.put("p-ride", rideid);

        db.collection("users").document(fbuser.getUid())
                .update(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

        Toast.makeText(PaymentActivity.this, "You have joined a ride which will take place on " + "rideDataList.get(position).date" + " at " + "rideDataList.get(position).time",
                Toast.LENGTH_LONG).show();
        finish();
    }

    void postNonceToServer(String nonce) {

        //#######POST THE NONCE TO THE SERVER USING A DIFFERENT BUTTON
        //JUST SAVE THE DETAILS THEN DO IT

        AsyncHttpClient client = new AsyncHttpClient();

        RequestParams params = new RequestParams();

        params.put("payment_method_nonce", nonce);
        params.put("amount", price);

        client.post("https://riderr-test.herokuapp.com/checkouts", params,

                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] b) {
                        System.out.println(statusCode);
                        AddUserToRide();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] b, Throwable throwable) {
                        System.out.println(statusCode);
                    }
                }
        );

    }
}
