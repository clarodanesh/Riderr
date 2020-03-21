package com.riderrapp.riderr;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
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
import com.mapbox.mapboxsdk.Mapbox;
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
import java.util.Calendar;
import java.util.Date;

public class SearchActivity extends AppCompatActivity{

    private static final String TAG = "SearchActivity";

    //creating variables for buttons, textview and integers to use
    Button pickDateBtn, pickTimeBtn;
    TextView dateText, timeText;
    private int theYear, theMonth, theDay, theHour, theMinute;
    private String fullDate, fullTime, destination, lng, lat;
    private String p, d;

    //Mapbox places plugin implementation
    private static final int RC_AC = 1;

    final FirebaseFirestore dataStore = FirebaseFirestore.getInstance();
    final FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ActionBar topBar = getSupportActionBar();
        topBar.setDisplayHomeAsUpEnabled(true);
        Mapbox.getInstance(this, getString(R.string.access_token));

        final Intent foundRidesIntent = new Intent(this, FoundRidesActivity.class);

        GetUserLocationDetails();

        String uid = currUser.getUid();

        DocumentReference userReference = dataStore.collection("users").document(uid);
        userReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> returnedTask) {
                if (returnedTask.isSuccessful()) {
                    DocumentSnapshot userDoc = returnedTask.getResult();
                    if (userDoc.exists()) {
                        p = userDoc.getString("p-ride");
                        d = userDoc.getString("d-ride");
                    } else {
                        Log.d(TAG, "couldnt get the users doc");
                    }
                } else {
                    Log.d(TAG, "Exception: ", returnedTask.getException());
                }
            }
        });

        //handle search ride button
        final Button searchRideBtn = (Button) findViewById(R.id.searchRideBtn);
        searchRideBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText searchRideTxtBox = (EditText)findViewById(R.id.searchTxtBox);

                if(IsDataCorrect(fullDate, fullTime, destination) && !UserHasRide()) {
                    if(IsLocationSet(lng, lat)) {
                        foundRidesIntent.putExtra(FoundRidesActivity.SEARCH_PLACE, searchRideTxtBox.getText().toString());
                        foundRidesIntent.putExtra(FoundRidesActivity.SEARCH_DATE, fullDate);
                        foundRidesIntent.putExtra(FoundRidesActivity.SEARCH_TIME, fullTime);
                        startActivity(foundRidesIntent);
                    }else{
                        Toast.makeText(SearchActivity.this, "You need to update your location in your profile", Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    Toast.makeText(SearchActivity.this, "Fill the form properly before submitting it.", Toast.LENGTH_LONG).show();
                }
            }
        });

        //get the views and assign to the buttons and text view variables
        pickDateBtn = (Button)findViewById(R.id.dateBtn);
        pickTimeBtn = (Button)findViewById(R.id.timeBtn);
        dateText = (TextView) findViewById(R.id.searchDate);
        timeText = (TextView) findViewById(R.id.timeLbl);

        pickDateBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // need to get the year month and day of the date
                final Calendar c = Calendar.getInstance();
                theYear = c.get(Calendar.YEAR);
                theMonth = c.get(Calendar.MONTH);
                theDay = c.get(Calendar.DAY_OF_MONTH);

                // handle date picker dialogue here by launching it
                DatePickerDialog dpDialog = new DatePickerDialog(SearchActivity.this,
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
                TimePickerDialog tpDialog = new TimePickerDialog(SearchActivity.this,
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

        BuildDestSearch();
    }

    private boolean UserHasRide(){
        if(p == null && d == null){
            return false;
        } else if (p == null && d != null) {
            Toast.makeText(SearchActivity.this, "You already have a Driver ride set.", Toast.LENGTH_LONG).show();
            return true;
        }else if(d == null && p != null){
            Toast.makeText(SearchActivity.this, "You already have a Passenger ride set.", Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

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

        if(userYear >= currYear){
            if(userMonth >= currMonth){
                if(userDay >= currDay || (userMonth > currMonth && userDay <= currDay)){
                    return true;
                }else{
                    Toast.makeText(SearchActivity.this, "The date you selected has passed.", Toast.LENGTH_LONG).show();
                    return false;
                }
            }else{
                Toast.makeText(SearchActivity.this, "The date you selected has passed.", Toast.LENGTH_LONG).show();
                return false;
            }
        }else{
            Toast.makeText(SearchActivity.this, "The date you selected has passed.", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean IsDateToday(String date){
        Date c = Calendar.getInstance().getTime();
        System.out.println("CAL TIME: " + c);

        SimpleDateFormat dFormatter = new SimpleDateFormat("d/M/yyyy");
        String dateFormatted = dFormatter.format(c);
        String[] currDate = dateFormatted.split("/");

        String[] usdArray = date.split("/");
        int userYear = Integer.parseInt(usdArray[2]);
        int userMonth = Integer.parseInt(usdArray[1]);
        int userDay = Integer.parseInt(usdArray[0]);
        int currYear = Integer.parseInt(currDate[2]);
        int currMonth = Integer.parseInt(currDate[1]);
        int currDay = Integer.parseInt(currDate[0]);

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

    private boolean IsTimeFuture(String time) {
        int currHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int currMin = Calendar.getInstance().get(Calendar.MINUTE);
        System.out.println("formatted time" + String.format("%02d:%02d", currHour, currMin));

        String[] ustArray = time.split(":");
        int userMinute = Integer.parseInt(ustArray[1]);
        int userHour = Integer.parseInt(ustArray[0]);

        if(userHour >= currHour){
            if(userMinute > currMin || (userHour > currHour && userMinute <= currMin)){
                return true;
            }else{
                Toast.makeText(SearchActivity.this, "This time has passed.", Toast.LENGTH_LONG).show();
                return false;
            }
        }else{
            Toast.makeText(SearchActivity.this, "This time has passed.", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void GetUserLocationDetails(){
        final String uid = currUser.getUid();
        DocumentReference userReference = dataStore.collection("users").document(uid);
        userReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> returnedTask) {
                if (returnedTask.isSuccessful()) {
                    DocumentSnapshot userDoc = returnedTask.getResult();
                    if (userDoc.exists()) {
                        lng = userDoc.getString("longitude");
                        lat = userDoc.getString("latitude");
                    } else {
                        Log.d(TAG, "couldnt find the user");
                    }
                } else {
                    Log.d(TAG, "Exception: ", returnedTask.getException());
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

    private void BuildDestSearch() {
        findViewById(R.id.searchTxtBox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent destinationSearchIntent = new PlaceAutocomplete.IntentBuilder() //this intent will open the activity that shows search results, will set up as a start activity for result, meaning it will close after result
                        .accessToken(getString(R.string.access_token))
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#FFFFFF"))
                                .toolbarColor(ContextCompat.getColor(SearchActivity.this, R.color.colorAccent))
                                .hint("Tap to Search")
                                .limit(10)
                                .country("GB") //ISO 3166 alpha 2 country codes separated by commas
                                .build(PlaceOptions.MODE_FULLSCREEN))
                        .build(SearchActivity.this);
                startActivityForResult(destinationSearchIntent, RC_AC);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == RC_AC) {
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
