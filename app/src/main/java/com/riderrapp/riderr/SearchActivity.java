package com.riderrapp.riderr;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;

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
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener{

    //creating variables for buttons, textview and integers to use
    Button pickDateBtn, pickTimeBtn;
    TextView dateText, timeText;
    private int theYear, theMonth, theDay, theHour, theMinute;
    private String fullDate, fullTime, destination;

    //Mapbox places plugin implementation
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private CarmenFeature home;
    private CarmenFeature work;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";
    private String country, dateTimeStamp, offeredBy, place, region, placeName;
    private double longitude, latitude;
    private int vehicleCapacity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        Mapbox.getInstance(this, getString(R.string.access_token));

        final Intent FoundRidesIntent = new Intent(this, FoundRidesActivity.class);

        //handle search ride button
        final Button searchRideBtn = (Button) findViewById(R.id.searchRideBtn);
        searchRideBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                //startActivity(intent);
                EditText searchRideTxtBox = (EditText)findViewById(R.id.searchTxtBox);

                if(IsDataFilled(fullDate, fullTime, destination)) {
                    FoundRidesIntent.putExtra(FoundRidesActivity.SEARCH_PLACE, searchRideTxtBox.getText().toString());
                    FoundRidesIntent.putExtra(FoundRidesActivity.SEARCH_DATE, fullDate);
                    FoundRidesIntent.putExtra(FoundRidesActivity.SEARCH_TIME, fullTime);
                    startActivity(FoundRidesIntent);
                }
                else{
                    Toast.makeText(SearchActivity.this, "Fill the entire form before trying to submit",
                            Toast.LENGTH_LONG).show();
                }
                //here can onclick get the fulldate and time and onlclick send to server
                //dateText.setText(fullDate);
            }
        });

        //get the views and assign to the buttons and text view variables
        pickDateBtn=(Button)findViewById(R.id.btn_date);
        pickTimeBtn=(Button)findViewById(R.id.btn_time);
        dateText=(TextView) findViewById(R.id.in_date);
        timeText=(TextView) findViewById(R.id.in_time);

        fullDate = "";
        fullTime = "";
        destination = "";

        //set on click listeners for the buttons
        pickDateBtn.setOnClickListener(this);
        pickTimeBtn.setOnClickListener(this);

        InitDestinationSearch();
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

    private void InitDestinationSearch() {
        findViewById(R.id.searchTxtBox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new PlaceAutocomplete.IntentBuilder() //this intent will open the activity that shows search results, will set up as a start activity for result, meaning it will close after result
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#FFFFFF"))
                                .toolbarColor(ContextCompat.getColor(SearchActivity.this, R.color.colorAccent))
                                .hint("Tap to Search")
                                .limit(10)
                                /*.addInjectedFeature(home)
                                .addInjectedFeature(work)*/
                                .build(PlaceOptions.MODE_FULLSCREEN))
                        .build(SearchActivity.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            EditText searchRideTxtBox = (EditText)findViewById(R.id.searchTxtBox);

            destination = PlaceAutocomplete.getPlace(data).text();

            searchRideTxtBox.setText(destination);
        }
    }

    private boolean IsDataFilled(String d, String t, String dest){
        if(d == "" || d.isEmpty()){
            return false;
        }
        if(t == "" || t.isEmpty()){
            return false;
        }
        if(dest == "" || dest.isEmpty()){
            return false;
        }
        return true;
    }
}
