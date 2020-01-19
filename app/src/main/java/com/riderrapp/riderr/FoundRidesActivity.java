package com.riderrapp.riderr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.content.Intent;
import android.widget.Toast;

public class FoundRidesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FoundRidesAdapter ridesAdapter;
    public List<rideData> rideDataList = new ArrayList<>();

    public static final String SEARCH_PLACE = "searchTerm";
    public static final String SEARCH_DATE = "searchDate";
    public static final String SEARCH_TIME = "searchTime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found_rides);

        recyclerView = findViewById(R.id.rides_recycler);
        String data = "Danesh";
        ridesAdapter = new FoundRidesAdapter(rideDataList);
        RecyclerView.LayoutManager manager = new GridLayoutManager(this, 1);
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
        RideDataPrepare();
    }

    private void RideDataPrepare() {
        String searchPlace = (String)getIntent().getExtras().get(SEARCH_PLACE);
        String searchDate = (String)getIntent().getExtras().get(SEARCH_DATE);
        String searchTime = (String)getIntent().getExtras().get(SEARCH_TIME);

        rideData data = new rideData(searchPlace, searchDate, searchTime, "check");
        rideDataList.add(data);
        data = new rideData("sai", "test", "test", "check");
        rideDataList.add(data);
        data = new rideData("sai", "test", "test", "check");
        rideDataList.add(data);
        data = new rideData("sai", "test", "test", "check");
        rideDataList.add(data);
        data = new rideData("sai", "test", "test", "check");
        rideDataList.add(data);
        data = new rideData("sai", "test", "test", "check");
        rideDataList.add(data);
        data = new rideData("sai", "test", "test", "check");
        rideDataList.add(data);
        data = new rideData("sai", "test", "test", "check");
        rideDataList.add(data);
        data = new rideData("sai", "test", "test", "check");
        rideDataList.add(data);
    }
}
