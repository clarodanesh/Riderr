package com.riderrapp.riderr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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

    //member variables for the payment activity class
    public static final String LONGITUDE = "lng";
    public static final String LATITUDE = "lat";
    public static final String RATING = "rating";
    public static final String AMOUNT_OF_RATINGS = "amt_of_ratings";
    public static final String RIDE_ID = "rideid";
    public static final String PRICE = "price";
    public static final String TIME = "time";
    public static final String DATE = "date";
    public static final String DEST = "dest";
    private static final String TAG = "PaymentActivity";
    private Map<String, Object> rideMap = new HashMap<>();
    private String cT;
    final int REQUEST_CODE = 1;
    AsyncHttpClient paymentClient = new AsyncHttpClient();
    String price;
    final FirebaseFirestore dataStore = FirebaseFirestore.getInstance();
    final FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle instanceState) {
        super.onCreate(instanceState);
        setContentView(R.layout.activity_payment);
        ActionBar topBar = getSupportActionBar();
        topBar.setDisplayHomeAsUpEnabled(true);

        //payment button needs to be set to button object
        final Button payBtn = (Button) findViewById(R.id.payBtn);
        //whilst payment is loading set the text to this and then disable the button
        payBtn.setText("LOADING PAYMENT...");
        payBtn.setEnabled(false);

        //BRAINTREE PAYMENT API SANDBOX VERSION
        //need to send a get request tp the demo server to get a client token for payments
        paymentClient.get("https://riderr-test.herokuapp.com/checkouts/new", new TextHttpResponseHandler() {
            @Override
            public void onSuccess(int sCode, cz.msebera.android.httpclient.Header[] h, String clientToken) {
                //onsucess need to set the client token to a variable
                cT = clientToken;
                cT = cT.replace("\"","");
                //set the payment button text to pay so user knows and set enabled to true
                payBtn.setText("PAY");
                payBtn.setEnabled(true);
            }

            @Override
            public void onFailure(int sCode, Header[] h, String resString, Throwable t) {
                //if the payment client fails then show a dialog
                AlertDialog errorDialog = new AlertDialog.Builder(new ContextThemeWrapper(PaymentActivity.this, R.style.NavAlerts)).create();
                errorDialog.setMessage("Payment client could not load.");
                errorDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"CLOSE", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dInterface, int num) {
                        finish();
                    }
                });

                errorDialog.show();
            }
        });

        //get the price from found rides activity
        price = (String)getIntent().getExtras().get(PRICE);

        //populate the text views on the payment activity
        PopulateTextViews();
    }

    public void PopulateTextViews(){
        TextView destLabel = (TextView)findViewById(R.id.DestinationLabel);
        TextView dateLabel = (TextView)findViewById(R.id.DateLabel);
        TextView timeLabel = (TextView)findViewById(R.id.TimeLabel);
        TextView priceLabel = (TextView)findViewById(R.id.PriceLabel);

        //set the text using the details from the found rides activity payment intent
        destLabel.setText((String)getIntent().getExtras().get(DEST));
        dateLabel.setText((String)getIntent().getExtras().get(DATE));
        timeLabel.setText((String)getIntent().getExtras().get(TIME));
        priceLabel.setText("£ " + (String)getIntent().getExtras().get(PRICE));
    }

    //BRAINTREE API METHOD
    //method from the braintree api used to open the drop in ui
    public void onBraintreeSubmit(View v) {
        DropInRequest req = new DropInRequest().clientToken(cT);
        req.disableGooglePayment(); //just want to see card method so remove paypal and googlepay
        req.disablePayPal();
        //open the drop in ui
        startActivityForResult(req.getIntent(this), REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent d) {
        super.onActivityResult(reqCode, resCode, d);
        //if the result was ok then need to send the nonce to the server
        if (reqCode == REQUEST_CODE) {
            if (resCode == RESULT_OK) {
                DropInResult result = d.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);

                //get the nonce to send to the server
                PaymentMethodNonce pmn = result.getPaymentMethodNonce();

                //could use this to get a drawable to show the card type
                //PaymentMethodType pmt = result.getPaymentMethodType();

                String nonceForServer;
                if(pmn != null){
                    nonceForServer = pmn.getNonce();
                }else{
                    nonceForServer = null;
                }

                //send the nonce to the server
                SendNonceToServer(nonceForServer);
            } else if (resCode == RESULT_CANCELED) {
                //if the user cancels the payment ui then show a dialog
                AlertDialog cancelDialog = new AlertDialog.Builder(new ContextThemeWrapper(PaymentActivity.this, R.style.NavAlerts)).create();
                cancelDialog.setMessage("Payment cancelled");
                cancelDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"OK", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dInterface, int num) {
                        finish();
                    }
                });

                cancelDialog.show();
            } else {
                Exception error = (Exception) d.getSerializableExtra(DropInActivity.EXTRA_ERROR);
            }
        }
    }

    //Once the user completes the payment then add the user to the ride and the ride to the user
    public void AddUserToRide(){
        String rideid = (String)getIntent().getExtras().get(RIDE_ID);
        String lng = (String)getIntent().getExtras().get(LONGITUDE);
        String lat = (String)getIntent().getExtras().get(LATITUDE);
        long rating = (long)getIntent().getExtras().get(RATING);
        long amtOfRatings = (long)getIntent().getExtras().get(AMOUNT_OF_RATINGS);


        //add the user details to the map
        rideMap.put("passenger", currUser.getUid());
        rideMap.put("longitude", lng);
        rideMap.put("latitude", lat);
        rideMap.put("rating", rating);
        rideMap.put("amountOfRatings", amtOfRatings);

        DocumentReference rideReference = dataStore.collection("OfferedRides").document(rideid);
        rideReference.update("passengers", FieldValue.arrayUnion(rideMap));
        rideReference.update("vehicleCapacity", FieldValue.increment(-1));

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("p-ride", rideid);

        //update the ride with the user details
        dataStore.collection("users").document(currUser.getUid())
                .update(userMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        Log.d(TAG, "Ride added to the user");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Exception: ", e);
                    }
                });

        Toast.makeText(PaymentActivity.this, "Awesome, you successfully joined the ride.", Toast.LENGTH_LONG).show();
        finish();
    }

    //send the nonce to the server so the user can be added to the ride
    void SendNonceToServer(String n) {
        paymentClient = new AsyncHttpClient();

        //need to create parameters that will contain a payment method nonce and amount
        RequestParams reqParameters = new RequestParams();

        reqParameters.put("payment_method_nonce", n);
        reqParameters.put("amount", price);

        //post the nonce to the server
        paymentClient.post("https://riderr-test.herokuapp.com/checkouts", reqParameters,

                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int sCode, cz.msebera.android.httpclient.Header[] h, byte[] b) {
                        System.out.println(sCode);
                        //add the user to the ride
                        AddUserToRide();
                    }

                    @Override
                    public void onFailure(int sCode, Header[] h, byte[] b, Throwable t) {
                        //if the nonce being sent to the server was failed then show a dialog
                        AlertDialog paymentFailedDialog = new AlertDialog.Builder(new ContextThemeWrapper(PaymentActivity.this, R.style.NavAlerts)).create();
                        paymentFailedDialog.setMessage("Sorry, it seems as though processing the payment failed");
                        paymentFailedDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"OK", new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dInterface, int num) {
                                finish();
                            }
                        });

                        paymentFailedDialog.show();
                    }
                }
        );
    }
}
