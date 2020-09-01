package com.riderrapp.riderr;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import android.widget.Button;

import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OfferRideActivity extends AppCompatActivity{

    //member variables for the offerrideactivity class
    private static final String TAG = "OfferRideActivity";
    Button pickDateBtn, pickTimeBtn;
    TextView dateText, timeText;
    private int theYear, theMonth, theDay, theHour, theMinute;
    protected String fullDate, fullTime, destination;
    private static final int RC_AC = 1;
    private String country, dateTimeStamp, offeredBy, place, region, placeName;
    private double longitude, latitude;
    private int vehicleCapacity;
    private String rprice;
    private String p, d, carMake, carReg, carPrice, fname, lname;
    private long carSeats, drating;
    final FirebaseFirestore dataStore = FirebaseFirestore.getInstance();
    final FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle instanceState) {
        super.onCreate(instanceState);
        setContentView(R.layout.activity_offer_ride);
        ActionBar topBar = getSupportActionBar();
        topBar.setDisplayHomeAsUpEnabled(true);
        //will be using the mapbox api so need to get an instance using my access token
        Mapbox.getInstance(this, getString(R.string.access_token));

        String uid = currUser.getUid();

        //need to get the user details from the db
        DocumentReference userReference = dataStore.collection("users").document(uid);
        userReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> returnedTask) {
                if (returnedTask.isSuccessful()) {
                    DocumentSnapshot userDoc = returnedTask.getResult();
                    if (userDoc.exists()) {
                        //set the details to the member variables for use later
                        p = userDoc.getString("p-ride");
                        d = userDoc.getString("d-ride");
                        carMake = userDoc.getString("car-make");
                        carReg = userDoc.getString("registration-no");
                        carSeats = userDoc.getLong("seats-no");
                        carPrice = userDoc.getString("ride-price");
                        fname = userDoc.getString("firstname");
                        lname = userDoc.getString("lastname");
                        drating = userDoc.getLong("rating");
                    } else {
                        Log.d(TAG, "Cant find the user");
                    }
                } else {
                    Log.d(TAG, "Exception: ", returnedTask.getException());
                }
            }
        });

        //when the offer ride button is pressed need to send the data to server and then close the activity
        final Button offerRideBtn = (Button) findViewById(R.id.offerRideBtn);
        offerRideBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //send the data for the ride from here
                if(IsDataCorrect(fullDate, fullTime, destination) && !UserHasRide() && CarDetailsSet() && IsNameSet()){
                    StoreData(country, longitude, latitude, fullDate, dateTimeStamp, destination, offeredBy, place, region, fullTime, vehicleCapacity, placeName, drating);
                    Toast.makeText(OfferRideActivity.this, "Your ride is now active", Toast.LENGTH_LONG).show();
                    finish();
                }
                else{
                    Toast.makeText(OfferRideActivity.this, "Please meet the requirement to offer ride.", Toast.LENGTH_LONG).show();
                }
            }
        });

        //get the views and assign to the buttons and text view variables
        pickDateBtn = (Button)findViewById(R.id.offerRideDateBtn);
        pickTimeBtn = (Button)findViewById(R.id.offerRideTimeBtn);
        dateText = (TextView) findViewById(R.id.offerRideDate);
        timeText = (TextView) findViewById(R.id.offerRideTime);

        pickDateBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //need to get the year month and day of the date
                final Calendar c = Calendar.getInstance();
                theYear = c.get(Calendar.YEAR);
                theMonth = c.get(Calendar.MONTH);
                theDay = c.get(Calendar.DAY_OF_MONTH);

                //handle date picker dialogue
                DatePickerDialog dpDialog = new DatePickerDialog(OfferRideActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker v, int y, int moy, int dom) {
                                fullDate = dom + "/" + (moy + 1) + "/" + y;
                                pickDateBtn.setText(fullDate);
                            }
                        }, theYear, theMonth, theDay);
                dpDialog.show();
            }
        });

        pickTimeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // need to get the hour and minute of current time
                final Calendar c = Calendar.getInstance();
                theHour = c.get(Calendar.HOUR_OF_DAY);
                theMinute = c.get(Calendar.MINUTE);

                // handle timpicker dialogue by launching it
                TimePickerDialog tpDialog = new TimePickerDialog(OfferRideActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker v, int hod, int m) {
                                fullTime = hod + ":" + m;
                                pickTimeBtn.setText(fullTime);
                            }
                        }, theHour, theMinute, false);
                tpDialog.show();
            }
        });

        fullDate = "";
        fullTime = "";
        destination = "";

        //need to get the user details and then build the destination search
        GetUserDetails();
        BuildDestSearch();
    }

    //need to make an intent which will return a result
    private void BuildDestSearch() {
        findViewById(R.id.offerRideTxtBox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Need to build the intent which will filter the country and only 10 results at a time
                Intent destinationSearchIntent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(getString(R.string.access_token))
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#FFFFFF"))
                                .toolbarColor(ContextCompat.getColor(OfferRideActivity.this, R.color.colorAccent))
                                .hint("Tap to Search")
                                .limit(10)
                                .country("GB")
                                .build(PlaceOptions.MODE_FULLSCREEN))
                        .build(OfferRideActivity.this);
                //open the activity and wait for a result
                startActivityForResult(destinationSearchIntent, RC_AC);
            }
        });
    }

    //activity result from before which will now be used to set details for the ride
    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent d) {
        super.onActivityResult(reqCode, resCode, d);
        //if the result was oke then i can use the returned data using a carmen feature
        if (resCode == Activity.RESULT_OK && reqCode == RC_AC) {
            EditText offerRideTxtBox = (EditText)findViewById(R.id.offerRideTxtBox);

            //this will get a carmen feature for the selected destination which holds all the data
            CarmenFeature selectedDest = PlaceAutocomplete.getPlace(d);

            //set all the data to the vars
            destination = PlaceAutocomplete.getPlace(d).text();
            LatLng latlng = new LatLng(((Point) selectedDest.geometry()).latitude(), ((Point) selectedDest.geometry()).longitude());
            latitude = latlng.getLatitude();
            longitude = latlng.getLongitude();
            placeName = PlaceAutocomplete.getPlace(d).placeName();

            offerRideTxtBox.setText(destination);

            //to access the carmen feature selectedCarmenFeature.context().get(1).id or .text etc...;
            //need to iterate through the result and set the region country and place
            if(selectedDest.context().size() > 0){
                for(int i = 0; i < selectedDest.context().size(); i++){
                    if(selectedDest.context().get(i).id().contains("region")){
                        region = selectedDest.context().get(i).text();
                    }
                    if(selectedDest.context().get(i).id().contains("country")){
                        country = selectedDest.context().get(i).text();
                    }
                    if(selectedDest.context().get(i).id().contains("place")){
                        place = selectedDest.context().get(i).text();
                    }else{
                        place = destination;
                    }
                }
            }
            //set date time stamp of when the ride was made
            Date dtStamp = Calendar.getInstance().getTime();
            dateTimeStamp = dtStamp.toString();
        }
    }

    //need to check if the date selected by the user is in the future
    //cant make a ride which is older
    private boolean IsDateFuture(String date){
        Date calTime = Calendar.getInstance().getTime();
        System.out.println("CAL TIME: " + calTime);

        SimpleDateFormat dFormatter = new SimpleDateFormat("d/M/yyyy");
        String dateFormatted = dFormatter.format(calTime);
        String[] currDate = dateFormatted.split("/");

        String[] usdArray = date.split("/");
        int userYear = Integer.parseInt(usdArray[2]);
        int userMonth = Integer.parseInt(usdArray[1]);
        int userDay = Integer.parseInt(usdArray[0]);
        int currYear = Integer.parseInt(currDate[2]);
        int currMonth = Integer.parseInt(currDate[1]);
        int currDay = Integer.parseInt(currDate[0]);

        //checking the year then month then day if ever false then show a toast
        if(userYear >= currYear){
            if(userMonth >= currMonth || (userYear > currYear && userMonth <= currMonth)){
                if(userDay >= currDay || (userMonth > currMonth && userDay <= currDay) || (userYear > currYear && userMonth <= currMonth && userDay <= currDay)){
                    return true;
                }else{
                    Toast.makeText(OfferRideActivity.this, "The date you selected has passed.", Toast.LENGTH_LONG).show();
                    return false;
                }
            }else{
                Toast.makeText(OfferRideActivity.this, "The date you selected has passed.", Toast.LENGTH_LONG).show();
                return false;
            }
        }else{
            Toast.makeText(OfferRideActivity.this, "The date you selected has passed.", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    //this method will check if the car details are set, if the details are not set then the user cant submit a ride
    private boolean CarDetailsSet(){
        if(carMake != "" && carPrice != "" && carReg != ""  && carSeats != 0){
            return true;
        }else{
            Toast.makeText(OfferRideActivity.this, "You need to fill in your car details to offer rides. Make sure your full name is set too.", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean IsNameSet(){
        if(!fname.equals("firstname") && !lname.equals("lastname")){
            return true;
        }else{
            Toast.makeText(OfferRideActivity.this, "You need to fill your name in on your profile.", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    //need to check if the date is today then dont need to check if date is future instead need to check if time is future
    private boolean IsDateToday(String date){
        Date calTime = Calendar.getInstance().getTime();

        SimpleDateFormat dFormatter = new SimpleDateFormat("d/M/yyyy");
        String dateFormatted = dFormatter.format(calTime);
        String[] currDate = dateFormatted.split("/");

        String[] usdArray = date.split("/");
        int userYear = Integer.parseInt(usdArray[2]);
        int userMonth = Integer.parseInt(usdArray[1]);
        int userDay = Integer.parseInt(usdArray[0]);
        int currYear = Integer.parseInt(currDate[2]);
        int currMonth = Integer.parseInt(currDate[1]);
        int currDay = Integer.parseInt(currDate[0]);

        //checking if the date is current day
        if(userYear == currYear){
            if(userMonth == currMonth){
                if(userDay == currDay){
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

    //need to check if the time is in the future same way checking the date
    private boolean IsTimeFuture(String t) {
        int currHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int currMin = Calendar.getInstance().get(Calendar.MINUTE);
        System.out.println("formatted time" + String.format("%02d:%02d", currHour, currMin));

        String[] ustArray = t.split(":");
        int userMinute = Integer.parseInt(ustArray[1]);
        int userHour = Integer.parseInt(ustArray[0]);

        if(userHour > currHour){
            if(userMinute > currMin || (userHour > currHour && userMinute <= currMin)){
                return true;
            }else{
                Toast.makeText(OfferRideActivity.this, "This time has passed.", Toast.LENGTH_LONG).show();
                return false;
            }
        }else{
            Toast.makeText(OfferRideActivity.this, "This time has passed.", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    //check if the user already has a dride or pride if they do then cant submit a new ride
    private boolean UserHasRide(){
        if(p == null && d == null){
            return false;
        } else if (p == null && d != null) {
            Toast.makeText(OfferRideActivity.this, "You already have a Driver ride set.", Toast.LENGTH_LONG).show();
            return true;
        }else if(d == null && p != null){
            Toast.makeText(OfferRideActivity.this, "You already have a Passenger ride set.", Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    private void CallErrorToast(String message){
        Toast.makeText(OfferRideActivity.this, message, Toast.LENGTH_LONG).show();
    }

    //before submitting the ride the data submitted needs to be validated
    private boolean IsDataCorrect(String d, String t, String dest){
        if(d == "" || d.isEmpty() || !IsDateFuture(d)){
            CallErrorToast("Date has to be filled.");
            return false;
        }
        if(t == "" || t.isEmpty()){
            CallErrorToast("Time cant be empty.");
            return false;
        }
        if(dest == "" || dest.isEmpty()){
            CallErrorToast("Destination cant be empty.");
            return false;
        }
        if(IsDateToday(d)){
            if(!IsTimeFuture(t)){
                return false;
            }
        }
        return true;
    }

    //get the user details from the db and set into member variables
    private void GetUserDetails(){
        String uid = currUser.getUid();
        offeredBy = uid;

        //using firebase firestore to get the details from the server
        DocumentReference userReference = dataStore.collection("users").document(uid);
        userReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot userDoc = task.getResult();
                    if (userDoc.exists()) {
                        vehicleCapacity = userDoc.getLong("seats-no").intValue();
                        rprice = userDoc.getString("ride-price");
                    } else {
                        Log.d(TAG, "couldnt get user");
                    }
                } else {
                    Log.d(TAG, "Exception: ", task.getException());
                }
            }
        });
    }

    private long GetRating(long r){
        if(r == -1){
            return 0;
        }else{
            return r;
        }
    }

    //store the data into a map then send to the server
    private void StoreData(String c, double lng, double lt, String dt, String stmp, String dst, String off, String pl, String rg, String t, int vcap, String pName, long dr){
        Map<String, Object> rideMap = new HashMap<>();
        rideMap.put("country", c);
        rideMap.put("date", dt);
        rideMap.put("dateTimeStamp", stmp);
        rideMap.put("destination", dst);
        rideMap.put("latitude", lt);
        rideMap.put("longitude", lng);
        rideMap.put("offeredBy", off);
        rideMap.put("place", pl);
        rideMap.put("region", rg);
        rideMap.put("time", t);
        rideMap.put("vehicleCapacity", vcap);
        rideMap.put("placeName", pName);
        rideMap.put("passengers", Arrays.asList());
        rideMap.put("completed", false);
        rideMap.put("ride-price", rprice);
        rideMap.put("ratingToShow", GetRating(dr));

        //send the map to the server
        //ride added to the db
        dataStore.collection("OfferedRides")
                .add(rideMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference rideDocRef) {
                        String uid = currUser.getUid();
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("d-ride", rideDocRef.getId());

                        dataStore.collection("users").document(uid)
                                .update(userMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void v) {
                                        Log.d(TAG, "driver ride added to user account");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Exception: ", e);
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Exception: ", e);
                    }
                });
    }
}
