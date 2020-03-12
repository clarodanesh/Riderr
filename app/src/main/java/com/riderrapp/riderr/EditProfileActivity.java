package com.riderrapp.riderr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    private double lat, lng;
    private String latToServer = null, lngToServer = null;

    private LocationManager mLocationManager;
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            System.out.println("Latitude changed to : " + location.getLatitude());
            System.out.println("Longitude changed to : " + location.getLongitude());
            //lat = location.getLatitude();
            //lng = location.getLongitude();
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            System.out.println("Location listener status just changed to : " + status);
        }
        @Override
        public void onProviderEnabled(String provider) {
            System.out.println("provider was just enabled : " + provider);
        }
        @Override
        public void onProviderDisabled(String provider) {
            System.out.println("provider was just disabled : " + provider);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        final Button saveProfileChangesBtn = (Button) findViewById(R.id.SaveProfileChangesBtn);
        final Button updateLocationBtn = (Button) findViewById(R.id.updateLocationBtn);

        if (ActivityCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditProfileActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }else{
            // Write you code here if permission already given.
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100,
                    1, mLocationListener);
        }

        updateLocationBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //latToServer = Double.toString(lat);
                //lngToServer = Double.toString(lng);
                if (ActivityCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(EditProfileActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    return;
                }else{
                    Location loc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    latToServer = Double.toString(loc.getLatitude());
                    lngToServer = Double.toString(loc.getLongitude());
                }
            }
        });

        saveProfileChangesBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SaveProfileChanges();
                finish();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        PopulateDetails();
    }

    private void CheckFirstTimeUser(String fname, String lname){
        //show the edit text features
        //if the name of the user is not set then they are first time user
        //if the name is set then disable the use of the name changing edit texts

        /* if(fname === notset && lname === notset){
            enable the edit text funct
        }
         */

        final EditText firstNameEdit =  (EditText) findViewById(R.id.editFirstname);
        final EditText lastNameEdit =  (EditText) findViewById(R.id.editLastname);

        if(fname.equals("firstname") && lname.equals("lastname")){
            firstNameEdit.setEnabled(true);
            lastNameEdit.setEnabled(true);
        }
        else if(fname.equals("firstname")){
            firstNameEdit.setEnabled(true);
            lastNameEdit.setEnabled(false);
        }
        else if(lname.equals("lastname")){
            firstNameEdit.setEnabled(false);
            lastNameEdit.setEnabled(true);
        }
        else{
            firstNameEdit.setEnabled(false);
            lastNameEdit.setEnabled(false);
        }
    }

    private void PopulateDetails(){
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

                        final EditText firstNameEdit =  (EditText) findViewById(R.id.editFirstname);
                        final EditText lastNameEdit =  (EditText) findViewById(R.id.editLastname);
                        final EditText carMakeEdit =  (EditText) findViewById(R.id.editCarMake);
                        final EditText carRegEdit =  (EditText) findViewById(R.id.editCarReg);
                        final EditText carSeatsEdit =  (EditText) findViewById(R.id.editCarSeats);
                        final EditText priceEdit =  (EditText) findViewById(R.id.editPrice);

                        firstNameEdit.setHint(document.getString("firstname"));
                        lastNameEdit.setHint(document.getString("lastname"));
                        carMakeEdit.setHint(document.getString("car-make"));
                        carRegEdit.setHint(document.getString("registration-no"));
                        priceEdit.setHint(document.getString("ride-price"));

                        int seatsNo = document.getLong("seats-no").intValue();
                        String seatsNoString = String.valueOf(seatsNo);
                        carSeatsEdit.setHint(seatsNoString);

                        CheckFirstTimeUser(firstNameEdit.getHint().toString(), lastNameEdit.getHint().toString());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void SaveProfileChanges(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = fbuser.getUid();
        final EditText firstNameText =  (EditText) findViewById(R.id.editFirstname);
        final EditText lastNameText =  (EditText) findViewById(R.id.editLastname);
        final EditText carMakeText =  (EditText) findViewById(R.id.editCarMake);
        final EditText carRegText =  (EditText) findViewById(R.id.editCarReg);
        final EditText carSeatsText =  (EditText) findViewById(R.id.editCarSeats);
        final EditText priceText =  (EditText) findViewById(R.id.editPrice);

        Map<String, Object> user = new HashMap<>();
        /*user.put("user-id", uid);
        user.put("car-make", "Toyota");
        user.put("registration-no", "FF55 LMN");
        user.put("seats-no", 4);
        user.put("user-email", "email@email.com");
        user.put("firstname", "Danesh");
        user.put("lastname", "Iqbal");*/

        if(!TextUtils.isEmpty(firstNameText.getText().toString())){
            user.put("firstname", firstNameText.getText().toString());
        }
        if(!TextUtils.isEmpty(lastNameText.getText().toString())){
            user.put("lastname", lastNameText.getText().toString());
        }
        if(!TextUtils.isEmpty(carMakeText.getText().toString())){
            user.put("car-make", carMakeText.getText().toString());
        }
        if(!TextUtils.isEmpty(carRegText.getText().toString())){
            user.put("registration-no", carRegText.getText().toString());
        }
        if(!TextUtils.isEmpty(carSeatsText.getText().toString())){
            int n = Integer.parseInt(carSeatsText.getText().toString());
            user.put("seats-no", n);
        }
        if(!TextUtils.isEmpty(priceText.getText().toString())){
            user.put("ride-price", priceText.getText().toString());
        }
        if(latToServer != null){
            user.put("latitude", latToServer);
            user.put("longitude", lngToServer);
        }

        //check if name and lastname for the user are disabled
        //user.put("firstname", firstNameText.getText().toString());
        //user.put("lastname", lastNameText.getText().toString());

        // Add a new document with a generated ID
        db.collection("users").document(uid)
                .update(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }
}
