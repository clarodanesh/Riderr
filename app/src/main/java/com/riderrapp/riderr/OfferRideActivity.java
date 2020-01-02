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
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

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
import java.util.HashMap;
import java.util.Map;

public class OfferRideActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "OfferRideActivity";

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


    //String c, double lng, double lt, String dt, String stmp, String dst, String off, String pl, String rg, String t, int vcap

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_ride);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        Mapbox.getInstance(this, getString(R.string.access_token));


        final Button offerRideBtn = (Button) findViewById(R.id.offerRideBtn);
        offerRideBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                //startActivity(intent);

                //here can onclick get the fulldate and time and onclick send to server
                //dateText.setText(fullDate);

                //send the data for the ride from here
                if(IsDataFilled(fullDate, fullTime, destination)){
                    StoreData(country, longitude, latitude, fullDate, dateTimeStamp, destination, offeredBy, place, region, fullTime, vehicleCapacity, placeName);
                }
                else{
                    Toast.makeText(OfferRideActivity.this, "Fill the entire form before trying to submit",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        //get the views and assign to the buttons and text view variables
        pickDateBtn=(Button)findViewById(R.id.btn_offer_ride_date);
        pickTimeBtn=(Button)findViewById(R.id.btn_offer_ride_time);
        dateText=(TextView) findViewById(R.id.offer_ride_date);
        timeText=(TextView) findViewById(R.id.offer_ride_time);

        fullDate = "";
        fullTime = "";
        destination = "";

        //set on click listeners for the buttons
        pickDateBtn.setOnClickListener(this);
        pickTimeBtn.setOnClickListener(this);

        GetUserDetails();
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
        findViewById(R.id.offerRideTxtBox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new PlaceAutocomplete.IntentBuilder() //this intent will open the activity that shows search results, will set up as a start activity for result, meaning it will close after result
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#FFFFFF"))
                                .limit(10)
                                /*.addInjectedFeature(home)
                                .addInjectedFeature(work)*/
                                .build(PlaceOptions.MODE_FULLSCREEN))
                        .build(OfferRideActivity.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            EditText offerRideTxtBox = (EditText)findViewById(R.id.offerRideTxtBox);

            // Retrieve selected location's CarmenFeature
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);
            //String add1 = PlaceAutocomplete.getPlace(data).placeName();
            destination = PlaceAutocomplete.getPlace(data).text();
            LatLng latlng = new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                    ((Point) selectedCarmenFeature.geometry()).longitude());
            latitude = latlng.getLatitude();
            longitude = latlng.getLongitude();
            placeName = PlaceAutocomplete.getPlace(data).placeName();

            offerRideTxtBox.setText(destination);

            //to access the carmen feature selectedCarmenFeature.context().get(1).id or .text and whatnot;
            if(selectedCarmenFeature.context().size() > 0){
                for(int i = 0; i < selectedCarmenFeature.context().size(); i++){
                    if(selectedCarmenFeature.context().get(i).id().contains("region")){
                        region = selectedCarmenFeature.context().get(i).text();
                    }
                    if(selectedCarmenFeature.context().get(i).id().contains("country")){
                        country = selectedCarmenFeature.context().get(i).text();
                    }
                    if(selectedCarmenFeature.context().get(i).id().contains("place")){
                        place = selectedCarmenFeature.context().get(i).text();
                    }else{
                        place = destination;
                    }
                }
            }


            //ed.setText(region);
            //lt.setText(ctry);
            //lg.setText(plce);

            Date dtStamp = Calendar.getInstance().getTime();
            dateTimeStamp = dtStamp.toString();

            /*
            Firebase.upload(
                string country;
                double long and lat;
                string date
                string dateTime stamp
                string destination
                string offeredBy : uuid
                string place
                string region
                string time
                int vehicleCapacity
            )
            * */

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

    private void GetUserDetails(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = fbuser.getUid();
        offeredBy = uid;

        DocumentReference docRef = db.collection("users").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getString("car-make"));

                        vehicleCapacity = document.getLong("seats-no").intValue();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void StoreData(String c, double lng, double lt, String dt, String stmp, String dst, String off, String pl, String rg, String t, int vcap, String pName){
        //SEND EVERYTHING TO SERVER
        //FOR used id and vehicle capacity create functions to get them from firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("country", c);
        data.put("date", dt);
        data.put("dateTimeStamp", stmp);
        data.put("destination", dst);
        data.put("latitude", lt);
        data.put("longitude", lng);
        data.put("offeredBy", off);
        data.put("place", pl);
        data.put("region", rg);
        data.put("time", t);
        data.put("vehicleCapacity", vcap);
        data.put("placeName", pName);

        db.collection("OfferedRides")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }
}
