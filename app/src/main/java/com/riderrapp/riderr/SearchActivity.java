package com.riderrapp.riderr;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "SearchActivity";

    //creating variables for buttons, textview and integers to use
    Button pickDateBtn, pickTimeBtn;
    TextView dateText, timeText;
    private int theYear, theMonth, theDay, theHour, theMinute;
    private String fullDate, fullTime, destination, lng, lat;
    private String p, d;

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

        GetUserLocationDetails();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = fbuser.getUid();

        DocumentReference docRef = db.collection("users").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getString("car-make"));

                        p = document.getString("p-ride");
                        d = document.getString("d-ride");
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        //handle search ride button
        final Button searchRideBtn = (Button) findViewById(R.id.searchRideBtn);
        searchRideBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                //startActivity(intent);
                EditText searchRideTxtBox = (EditText)findViewById(R.id.searchTxtBox);

                if(IsDataCorrect(fullDate, fullTime, destination) && !UserHasRide()) {
                    if(IsLocationSet(lng, lat)) {
                        FoundRidesIntent.putExtra(FoundRidesActivity.SEARCH_PLACE, searchRideTxtBox.getText().toString());
                        FoundRidesIntent.putExtra(FoundRidesActivity.SEARCH_DATE, fullDate);
                        FoundRidesIntent.putExtra(FoundRidesActivity.SEARCH_TIME, fullTime);
                        startActivity(FoundRidesIntent);
                    }else{
                        Toast.makeText(SearchActivity.this, "You need to update your location in your profile",
                                Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    Toast.makeText(SearchActivity.this, "Fill the form properly before submitting it.",
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

    private boolean UserHasRide(){
        if(p == null && d == null){
            return false;
        } else if (p == null && d != null) {
            Toast.makeText(SearchActivity.this, "You already have a Driver ride set.",
                    Toast.LENGTH_LONG).show();
            return true;
        }else if(d == null && p != null){
            Toast.makeText(SearchActivity.this, "You already have a Passenger ride set.",
                    Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    private boolean IsDateFuture(String date){
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("d/M/yyyy");
        String formattedDate = df.format(c);
        String[] fDate = formattedDate.split("/");

        String[] usdArray = date.split("/");
        int userYear = Integer.parseInt(usdArray[2]);
        int userMonth = Integer.parseInt(usdArray[1]);
        int userDay = Integer.parseInt(usdArray[0]);
        int currYear = Integer.parseInt(fDate[2]);
        int currMonth = Integer.parseInt(fDate[1]);
        int currDay = Integer.parseInt(fDate[0]);

        if(userYear >= currYear){
            //echo '<br>first<br>';
            //echo $currDateints[0];
            if(userMonth >= currMonth){
                //echo '<br>second<br>';
                //echo $currDateints[1];
                if(userDay >= currDay || (userMonth > currMonth && userDay <= currDay)){
                    //echo '<br>third<br>';
                    //echo $currDateints[2];
                    return true;
                }else{
                    return false;
                }
            }else{
                return false;
            }
        }else{
            return false;
        }

    }

    private boolean IsDateToday(String date){
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("d/M/yyyy");
        String formattedDate = df.format(c);
        String[] fDate = formattedDate.split("/");

        String[] usdArray = date.split("/");
        int userYear = Integer.parseInt(usdArray[2]);
        int userMonth = Integer.parseInt(usdArray[1]);
        int userDay = Integer.parseInt(usdArray[0]);
        int currYear = Integer.parseInt(fDate[2]);
        int currMonth = Integer.parseInt(fDate[1]);
        int currDay = Integer.parseInt(fDate[0]);

        if(userYear == currYear){
            //echo '<br>first<br>';
            //echo $currDateints[0];
            if(userMonth == currMonth){
                //echo '<br>second<br>';
                //echo $currDateints[1];
                if(userDay == currDay){
                    //echo '<br>third<br>';
                    //echo $currDateints[2];
                    return true;
                }else{
                    return false;
                }
            }else{
                return false;
            }
        }else{
            return false;
        }

    }

    private boolean IsTimeFuture(String time) {
        int currHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int currMin = Calendar.getInstance().get(Calendar.MINUTE);
        System.out.println("time_format" + String.format("%02d:%02d", currHour, currMin));

        String[] ustArray = time.split(":");
        int userMinute = Integer.parseInt(ustArray[1]);
        int userHour = Integer.parseInt(ustArray[0]);

        if(userHour >= currHour){
            //echo '<br>first<br>';
            //echo $currDateints[0];
            if(userMinute >= currMin){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    private void GetUserLocationDetails(){
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = fbuser.getUid();
        DocumentReference docRef = db.collection("users").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        lng = document.getString("longitude");
                        lat = document.getString("latitude");
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private boolean IsLocationSet(String lg, String lt){
        if(lg == null){
            return false;
        }
        if(lt == null){
            return false;
        }
        return true;
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
                                .country("GB") //ISO 3166 alpha 2 country codes separated by commas
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

    private boolean IsDataCorrect(String d, String t, String dest){
        if(d == "" || d.isEmpty() || !IsDateFuture(d)){
            return false;
        }
        if(t == "" || t.isEmpty()){
            return false;
        }
        if(dest == "" || dest.isEmpty()){
            return false;
        }
        if(IsDateToday(d)){
            if(!IsTimeFuture(t)){
                return false;
            }
        }
        return true;
    }
}
