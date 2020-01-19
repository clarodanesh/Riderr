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
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class FoundRidesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FoundRidesAdapter ridesAdapter;
    public List<rideData> rideDataList = new ArrayList<>();

    public static final String SEARCH_PLACE = "searchTerm";
    public static final String SEARCH_DATE = "searchDate";
    public static final String SEARCH_TIME = "searchTime";

    private static final String TAG = "FoundRidesActivity";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found_rides);


        mAuth = FirebaseAuth.getInstance();
        RideDataPrepare();
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

    private void RideDataPrepare() {
        String searchPlace = (String)getIntent().getExtras().get(SEARCH_PLACE);
        String searchDate = (String)getIntent().getExtras().get(SEARCH_DATE);
        String searchTime = (String)getIntent().getExtras().get(SEARCH_TIME);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = fbuser.getUid();

        //retrieve the offeredBy data and the amount of seats in the car data
        //CollectionReference docRef = db.collection("users");
        db.collection("OfferedRides")
                .whereEqualTo("place", searchPlace)
                .whereEqualTo("date", searchDate)
                .whereEqualTo("time", searchTime)
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

                                    Toast.makeText(FoundRidesActivity.this, rideDataList.get(position).place,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.get("place"));
                                Log.d(TAG, document.getId() + " => " + document.get("date"));
                                Log.d(TAG, document.getId() + " => " + document.get("time"));
                                data = new rideData(document.get("place").toString(), document.get("date").toString(), document.get("time").toString(), "check");
                                rideDataList.add(data);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}
