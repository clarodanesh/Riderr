package com.riderrapp.riderr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.braintreepayments.api.dropin.DropInRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;

public class FoundRidesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FoundRidesAdapter ridesAdapter;
    public List<rideData> rideDataList = new ArrayList<>();

    public static final String SEARCH_PLACE = "searchTerm";
    public static final String SEARCH_DATE = "searchDate";
    public static final String SEARCH_TIME = "searchTime";

    private static final String TAG = "FoundRidesActivity";

    private String lng;
    private String lat;
    private long rating;
    private long amtOfRatings;
    private Map<String, Object> uData = new HashMap<>();

    private FirebaseAuth mAuth;

    private String cT;
    final int REQUEST_CODE = 1;
    AsyncHttpClient client = new AsyncHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found_rides);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = fbuser.getUid();

        DocumentReference docRef = db.collection("users").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        lng = document.getString("longitude");
                        lat = document.getString("latitude");
                        amtOfRatings = document.getLong("amountOfRatings");
                        if(document.get("rating") == null) {
                            rating = -1;
                        }else{
                            rating = document.getLong("rating");
                        }
                        mAuth = FirebaseAuth.getInstance();
                        RideDataPrepare();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

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
    }

    @Override
    public void onStart() {
        super.onStart();
        GetUser();
    }

    private void GetUser(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            Toast.makeText(FoundRidesActivity.this, "User is signed in.",
                    Toast.LENGTH_SHORT).show();
        } else {
            // No user is signed in
            Toast.makeText(FoundRidesActivity.this, "NO user is signed in.",
                    Toast.LENGTH_SHORT).show();
        }
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

    private void RideDataPrepare() {
        String searchPlace = (String)getIntent().getExtras().get(SEARCH_PLACE);
        String searchDate = (String)getIntent().getExtras().get(SEARCH_DATE);
        String searchTime = (String)getIntent().getExtras().get(SEARCH_TIME);

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = fbuser.getUid();

        //retrieve the offeredBy data and the amount of seats in the car data
        //CollectionReference docRef = db.collection("users");
        db.collection("OfferedRides")
                .whereEqualTo("place", searchPlace)
                .whereEqualTo("date", searchDate)
                .whereEqualTo("time", searchTime)
                .whereGreaterThan("vehicleCapacity", 0)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            rideData data= null;
                            recyclerView = findViewById(R.id.rides_recycler);
                            ridesAdapter = new FoundRidesAdapter(rideDataList);
                            RecyclerView.LayoutManager manager = new GridLayoutManager(FoundRidesActivity.this, 1);
                            recyclerView.setLayoutManager(manager);
                            //recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
                            recyclerView.setAdapter(ridesAdapter);
                            ridesAdapter.setListener(new FoundRidesAdapter.Listener() {
                                public void onClick(int position) {
                                    //Intent intent = new Intent(FoundRidesActivity.this, MainActivity.class);
                                    //intent.putExtra(PizzaDetailActivity.EXTRA_PIZZA_ID, position);
                                    //intent.putExtra();
                                    //startActivity(intent);

                                    final Intent paymentIntent = new Intent(FoundRidesActivity.this, PaymentActivity.class);

                                    paymentIntent.putExtra(PaymentActivity.LONGITUDE, lng);
                                    paymentIntent.putExtra(PaymentActivity.LATITUDE, lat);
                                    paymentIntent.putExtra(PaymentActivity.RATING, rating);
                                    paymentIntent.putExtra(PaymentActivity.AMOUNT_OF_RATINGS, amtOfRatings);
                                    paymentIntent.putExtra(PaymentActivity.RIDE_ID, rideDataList.get(position).rideId);
                                    paymentIntent.putExtra(PaymentActivity.PRICE, rideDataList.get(position).price);
                                    paymentIntent.putExtra(PaymentActivity.TIME, rideDataList.get(position).time);
                                    paymentIntent.putExtra(PaymentActivity.DATE, rideDataList.get(position).date);
                                    paymentIntent.putExtra(PaymentActivity.DEST, rideDataList.get(position).place);
                                    startActivity(paymentIntent);

                                    /*uData.put("passenger", fbuser.getUid());
                                    uData.put("longitude", lng);
                                    uData.put("latitude", lat);
                                    uData.put("rating", rating);
                                    uData.put("amountOfRatings", amtOfRatings);

                                    //TODO ADD THE USER DATA TO THE RIDE HERE AND DECREMENT RIDE VCAP
                                    DocumentReference selectedRideRef = db.collection("OfferedRides").document(rideDataList.get(position).rideId);
                                    selectedRideRef.update("passengers", FieldValue.arrayUnion(uData));
                                    selectedRideRef.update("vehicleCapacity", FieldValue.increment(-1));

                                    Map<String, Object> user = new HashMap<>();
                                    user.put("p-ride", rideDataList.get(position).rideId);

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

                                    Toast.makeText(FoundRidesActivity.this, "You have joined a ride which will take place on " + rideDataList.get(position).date + " at " + rideDataList.get(position).time,
                                            Toast.LENGTH_LONG).show();
                                    finish();*/
                                }
                            });
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.get("place"));
                                Log.d(TAG, document.getId() + " => " + document.get("date"));
                                Log.d(TAG, document.getId() + " => " + document.get("time"));
                                if(document.getLong("vehicleCapacity").intValue() > 0 && document.getBoolean("completed") == false) {
                                    data = new rideData(document.get("place").toString(), document.get("date").toString(), document.get("time").toString(), document.get("offeredBy").toString(), document.getId(), document.getString("ride-price"));
                                    rideDataList.add(data);
                                }else{
                                    Log.d(TAG, "Ride not added due to vcap");
                                }
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}
