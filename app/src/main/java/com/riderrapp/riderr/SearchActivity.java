package com.riderrapp.riderr;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.View;

import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBar;

import android.view.Menu;
import android.widget.Button;

import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.TextView;

import java.util.Calendar;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener{

    //creating variables for buttons, textview and integers to use
    Button pickDateBtn, pickTimeBtn;
    TextView dateText, timeText;
    private int theYear, theMonth, theDay, theHour, theMinute;
    private String fullDate, fullTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        final Intent FoundRidesIntent = new Intent(this, FoundRidesActivity.class);

        //handle search ride button
        final Button searchRideBtn = (Button) findViewById(R.id.searchRideBtn);
        searchRideBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                //startActivity(intent);
                EditText offerRideTxtBox = (EditText)findViewById(R.id.searchTxtBox);
                FoundRidesIntent.putExtra(FoundRidesActivity.SEARCH_PLACE, offerRideTxtBox.getText().toString());
                FoundRidesIntent.putExtra(FoundRidesActivity.SEARCH_DATE, fullDate);
                FoundRidesIntent.putExtra(FoundRidesActivity.SEARCH_TIME, fullTime);
                //here can onclick get the fulldate and time and onlclick send to server
                //dateText.setText(fullDate);
                startActivity(FoundRidesIntent);
            }
        });

        //get the views and assign to the buttons and text view variables
        pickDateBtn=(Button)findViewById(R.id.btn_date);
        pickTimeBtn=(Button)findViewById(R.id.btn_time);
        dateText=(TextView) findViewById(R.id.in_date);
        timeText=(TextView) findViewById(R.id.in_time);

        //set on click listeners for the buttons
        pickDateBtn.setOnClickListener(this);
        pickTimeBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        //if the view is the pick date button then handle it here
        if (v == pickDateBtn) {

            // need to get the year month and day of the date
            final Calendar calendar = Calendar.getInstance();
            theYear = calendar.get(Calendar.YEAR);
            theMonth = calendar.get(Calendar.MONTH);
            theDay = calendar.get(Calendar.DAY_OF_MONTH);

            // handle date picker dialogue here by launching it
            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                            //dateText.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                            fullDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                            pickDateBtn.setText(fullDate);
                            //dateText.setText(fullDate);
                        }
                    }, theYear, theMonth, theDay);
            datePickerDialog.show();
        }

        //if the view being clicked is the pick time button then handle it here
        if (v == pickTimeBtn) {

            // need to get the hour and minute of current time
            final Calendar calendar = Calendar.getInstance();
            theHour = calendar.get(Calendar.HOUR_OF_DAY);
            theMinute = calendar.get(Calendar.MINUTE);

            // handle timpicker dialogue by launching it
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {

                            //timeText.setText(hourOfDay + ":" + minute);
                            fullTime = hourOfDay + ":" + minute;
                            pickTimeBtn.setText(fullTime);
                        }
                    }, theHour, theMinute, false);
            timePickerDialog.show();
        }
    }
}
