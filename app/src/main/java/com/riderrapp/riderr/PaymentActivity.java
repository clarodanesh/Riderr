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

        final Button payBtn = (Button) findViewById(R.id.payBtn);
        payBtn.setText("LOADING PAYMENT...");
        payBtn.setEnabled(false);

        paymentClient.get("https://riderr-test.herokuapp.com/checkouts/new", new TextHttpResponseHandler() {
            @Override
            public void onSuccess(int sCode, cz.msebera.android.httpclient.Header[] h, String clientToken) {
                cT = clientToken;
                cT = cT.replace("\"","");
                payBtn.setText("PAY");
                payBtn.setEnabled(true);
            }

            @Override
            public void onFailure(int sCode, Header[] h, String resString, Throwable t) {
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

        price = (String)getIntent().getExtras().get(PRICE);

        PopulateTextViews();
    }

    public void PopulateTextViews(){
        TextView destLabel = (TextView)findViewById(R.id.DestinationLabel);
        TextView dateLabel = (TextView)findViewById(R.id.DateLabel);
        TextView timeLabel = (TextView)findViewById(R.id.TimeLabel);
        TextView priceLabel = (TextView)findViewById(R.id.PriceLabel);

        destLabel.setText((String)getIntent().getExtras().get(DEST));
        dateLabel.setText((String)getIntent().getExtras().get(DATE));
        timeLabel.setText((String)getIntent().getExtras().get(TIME));
        priceLabel.setText("£ " + (String)getIntent().getExtras().get(PRICE));
    }

    public void onBraintreeSubmit(View v) {
        DropInRequest req = new DropInRequest().clientToken(cT);
        req.disableGooglePayment();
        req.disablePayPal();
        startActivityForResult(req.getIntent(this), REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent d) {
        super.onActivityResult(reqCode, resCode, d);
        if (reqCode == REQUEST_CODE) {
            if (resCode == RESULT_OK) {
                DropInResult result = d.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);

                PaymentMethodNonce pmn = result.getPaymentMethodNonce();

                PaymentMethodType pmt = result.getPaymentMethodType();

                String nonceForServer;
                if(pmn != null){
                    nonceForServer = pmn.getNonce();
                }else{
                    nonceForServer = null;
                }

                postNonceToServer(nonceForServer);
            } else if (resCode == RESULT_CANCELED) {
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

    public void AddUserToRide(){
        String rideid = (String)getIntent().getExtras().get(RIDE_ID);
        String lng = (String)getIntent().getExtras().get(LONGITUDE);
        String lat = (String)getIntent().getExtras().get(LATITUDE);
        long rating = (long)getIntent().getExtras().get(RATING);
        long amtOfRatings = (long)getIntent().getExtras().get(AMOUNT_OF_RATINGS);



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

    void postNonceToServer(String n) {
        paymentClient = new AsyncHttpClient();

        RequestParams reqParameters = new RequestParams();

        reqParameters.put("payment_method_nonce", n);
        reqParameters.put("amount", price);

        paymentClient.post("https://riderr-test.herokuapp.com/checkouts", reqParameters,

                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int sCode, cz.msebera.android.httpclient.Header[] h, byte[] b) {
                        System.out.println(sCode);
                        AddUserToRide();
                    }

                    @Override
                    public void onFailure(int sCode, Header[] h, byte[] b, Throwable t) {
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
