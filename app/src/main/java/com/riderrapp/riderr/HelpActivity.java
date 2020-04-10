package com.riderrapp.riderr;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Help activity made for anyone using this app
        //gives acces to dev email and lets user know how to mock payment with fake bank details
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        ActionBar topBar = getSupportActionBar();
        topBar.setDisplayHomeAsUpEnabled(true);
    }
}
