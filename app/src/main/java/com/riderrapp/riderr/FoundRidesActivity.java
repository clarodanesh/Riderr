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
    public List<studentData> studentDataList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_found_rides);

        recyclerView = findViewById(R.id.rides_recycler);
        String data = "Danesh";
        ridesAdapter = new FoundRidesAdapter(studentDataList);
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

                Toast.makeText(FoundRidesActivity.this, studentDataList.get(position).name,
                       Toast.LENGTH_SHORT).show();
            }
        });
        StudentDataPrepare();
    }

    private void StudentDataPrepare() {
        studentData data = new studentData("sai", 25);
        studentDataList.add(data);
        data = new studentData("sai", 25);
        studentDataList.add(data);
        data = new studentData("raghu", 20);
        studentDataList.add(data);
        data = new studentData("raj", 28);
        studentDataList.add(data);
        data = new studentData("amar", 15);
        studentDataList.add(data);
        data = new studentData("bapu", 19);
        studentDataList.add(data);
        data = new studentData("chandra", 52);
        studentDataList.add(data);
        data = new studentData("deraj", 30);
        studentDataList.add(data);
        data = new studentData("eshanth", 28);
        studentDataList.add(data);
    }
}
