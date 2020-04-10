package com.riderrapp.riderr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class FoundRidesActivity extends AppCompatActivity {

    //member variables of the FoundRidesActivity class
    private RecyclerView ridesRecyclerView;
    private FoundRidesAdapter ridesAdapter;
    public List<rideData> rideDataList = new ArrayList<>();
    public static final String SEARCH_PLACE = "searchPlace";
    public static final String SEARCH_DATE = "searchDate";
    public static final String SEARCH_TIME = "searchTime";
    private static final String TAG = "FoundRidesActivity";
    private String lng, lat;
    private long rating, amtOfRatings;
    final FirebaseFirestore dataStore = FirebaseFirestore.getInstance();
    final FirebaseUser authUser = FirebaseAuth.getInstance().getCurrentUser();
    String uid = authUser.getUid();

    //overriding the oncreate method from appcompatactivity
    @Override
    protected void onCreate(Bundle instanceState) {
        super.onCreate(instanceState);

        //set the layout for found rides
        setContentView(R.layout.activity_found_rides);

        //need to get the users details as soon as the activity is created, saves the user waiting around
        //using firebase firestore db functions to get the data
        DocumentReference usersReference = dataStore.collection("users").document(uid);
        usersReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> returnedTask) {
                if (returnedTask.isSuccessful()) {
                    DocumentSnapshot usersDocument = returnedTask.getResult();
                    if (usersDocument.exists()) {
                        lng = usersDocument.getString("longitude");
                        lat = usersDocument.getString("latitude");
                        amtOfRatings = usersDocument.getLong("amountOfRatings");
                        if(usersDocument.get("rating") == null) {
                            rating = -1;
                        }else{
                            rating = usersDocument.getLong("rating");
                        }

                        //need to prepare the ride data, this will retrieve data for all the rides and return them in card views
                        RideDataPrepare();
                    } else {
                        Toast.makeText(FoundRidesActivity.this, "Could not find user details.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "Exception: ", returnedTask.getException());
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    //method will prepare the ride data in card views with buttons that allow user to reach payment activity
    private void RideDataPrepare() {
        //on search activity I passed variables which relate to the users search terms
        //have to retrieve these from the intent that I sent to FoundridesActivity
        String searchPlace = (String)getIntent().getExtras().get(SEARCH_PLACE);
        String searchDate = (String)getIntent().getExtras().get(SEARCH_DATE);
        String searchTime = (String)getIntent().getExtras().get(SEARCH_TIME);

        //using firebase firestore query filter to retrieve data
        //retrieving all rides that contain users search place data and arrival time
        dataStore.collection("OfferedRides")
                .whereEqualTo("place", searchPlace)
                .whereEqualTo("date", searchDate)
                .whereEqualTo("time", searchTime)
                .whereGreaterThan("vehicleCapacity", 0)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> returnedTask) {
                        if (returnedTask.isSuccessful()) {
                            rideData rData = null;

                            //all of the inflated card views will go into a recyclerview so that
                            //multiple can be inflated and also the user can scroll
                            ridesRecyclerView = findViewById(R.id.ridesRecycler);
                            ridesAdapter = new FoundRidesAdapter(rideDataList);
                            RecyclerView.LayoutManager recyclerLayoutManager = new GridLayoutManager(FoundRidesActivity.this, 1);
                            ridesRecyclerView.setLayoutManager(recyclerLayoutManager);
                            ridesRecyclerView.setAdapter(ridesAdapter);

                            //need to set a click listener for the rides button which opens payment activity
                            ridesAdapter.setListener(new FoundRidesAdapter.Listener() {
                                public void onClick(int idx) {
                                    //need to pass the data of the chosen ride and the user to the payment activity
                                    //using an intent
                                    final Intent paymentIntent = new Intent(FoundRidesActivity.this, PaymentActivity.class);

                                    paymentIntent.putExtra(PaymentActivity.LONGITUDE, lng);
                                    paymentIntent.putExtra(PaymentActivity.LATITUDE, lat);
                                    paymentIntent.putExtra(PaymentActivity.RATING, rating);
                                    paymentIntent.putExtra(PaymentActivity.AMOUNT_OF_RATINGS, amtOfRatings);
                                    paymentIntent.putExtra(PaymentActivity.RIDE_ID, rideDataList.get(idx).rideId);
                                    paymentIntent.putExtra(PaymentActivity.PRICE, rideDataList.get(idx).price);
                                    paymentIntent.putExtra(PaymentActivity.TIME, rideDataList.get(idx).time);
                                    paymentIntent.putExtra(PaymentActivity.DATE, rideDataList.get(idx).date);
                                    paymentIntent.putExtra(PaymentActivity.DEST, rideDataList.get(idx).place);

                                    //start the payment activity with the extras
                                    startActivity(paymentIntent);
                                }
                            });

                            //from all the documents returned, user only needs to see the ones with space in the vehicle
                            //and that are not completed, so need to check for this
                            //will build the rideDataList using the data returned in the filtered documents
                            for (QueryDocumentSnapshot ridesDocument : returnedTask.getResult()) {
                                if(ridesDocument.getLong("vehicleCapacity").intValue() > 0 && ridesDocument.getBoolean("completed") == false) {
                                    rData = new rideData(ridesDocument.get("place").toString(), ridesDocument.get("date").toString(), ridesDocument.get("time").toString(), ridesDocument.get("offeredBy").toString(), ridesDocument.getId(), ridesDocument.getString("ride-price"), ridesDocument.get("ratingToShow").toString());
                                    rideDataList.add(rData);
                                }else{
                                    Log.d(TAG, "Ride not added due to vcap");
                                }
                            }
                        } else {
                            Log.d(TAG, "Exception: ", returnedTask.getException());
                        }
                    }
                });
    }
}
