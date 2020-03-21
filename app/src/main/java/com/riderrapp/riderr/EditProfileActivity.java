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
import android.widget.Toast;

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

public class EditProfileActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback{

    private static final String TAG = "EditProfileActivity";

    private String latToServer = null, lngToServer = null;

    private LocationManager locManager;

    FirebaseFirestore dataStore = FirebaseFirestore.getInstance();
    FirebaseUser authUser = FirebaseAuth.getInstance().getCurrentUser();
    String uid = authUser.getUid();

    private final LocationListener locListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location loc) {
            System.out.println("Latitude changed to : " + loc.getLatitude());
            System.out.println("Longitude changed to : " + loc.getLongitude());
        }
        @Override
        public void onStatusChanged(String p, int s, Bundle e) {
            System.out.println("Loc listener status: " + s);
        }
        @Override
        public void onProviderEnabled(String p) {
            System.out.println("Provides en : " + p);
        }
        @Override
        public void onProviderDisabled(String p) {
            System.out.println("Provider dis : " + p);
        }
    };

    public void onRequestPermissionsResult(int rCode, String perm[], int[] permissionsGranted) {
        if (ActivityCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Riderr needs to see your location", Toast.LENGTH_SHORT).show();
            return;
        }else{
            locManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100,
                    1, locListener);

            Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            latToServer = Double.toString(loc.getLatitude());
            lngToServer = Double.toString(loc.getLongitude());
        }
    }

    @Override
    protected void onCreate(Bundle instanceState) {
        super.onCreate(instanceState);
        setContentView(R.layout.activity_edit_profile);

        final Button saveProfileChangesBtn = (Button) findViewById(R.id.SaveProfileChangesBtn);
        final Button updateLocationBtn = (Button) findViewById(R.id.updateLocationBtn);

        updateLocationBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(EditProfileActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    return;
                }else{
                    locManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                    locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100,
                            1, locListener);

                    Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

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

        if (ActivityCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditProfileActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }else{
            locManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, locListener);
        }
    }

    private void CheckFirstTimeUser(String fname, String lname){
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
        dataStore = FirebaseFirestore.getInstance();
        authUser = FirebaseAuth.getInstance().getCurrentUser();
        uid = authUser.getUid();

        DocumentReference usersReference = dataStore.collection("users").document(uid);
        usersReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> returnedTask) {
                if (returnedTask.isSuccessful()) {
                    DocumentSnapshot usersDocument = returnedTask.getResult();
                    if (usersDocument.exists()) {
                        final EditText firstNameEdit =  (EditText) findViewById(R.id.editFirstname);
                        final EditText lastNameEdit =  (EditText) findViewById(R.id.editLastname);
                        final EditText carMakeEdit =  (EditText) findViewById(R.id.editCarMake);
                        final EditText carRegEdit =  (EditText) findViewById(R.id.editCarReg);
                        final EditText carSeatsEdit =  (EditText) findViewById(R.id.editCarSeats);
                        final EditText priceEdit =  (EditText) findViewById(R.id.editPrice);

                        firstNameEdit.setHint(usersDocument.getString("firstname"));
                        lastNameEdit.setHint(usersDocument.getString("lastname"));
                        carMakeEdit.setHint(usersDocument.getString("car-make"));
                        carRegEdit.setHint(usersDocument.getString("registration-no"));
                        priceEdit.setHint(usersDocument.getString("ride-price"));

                        int seatsNo = usersDocument.getLong("seats-no").intValue();
                        String seatsNoString = String.valueOf(seatsNo);
                        carSeatsEdit.setHint(seatsNoString);

                        CheckFirstTimeUser(firstNameEdit.getHint().toString(), lastNameEdit.getHint().toString());
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Could not find user details.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d(TAG, "Exception: ", returnedTask.getException());
                }
            }
        });
    }

    private void SaveProfileChanges(){
        dataStore = FirebaseFirestore.getInstance();
        authUser = FirebaseAuth.getInstance().getCurrentUser();
        uid = authUser.getUid();
        final EditText firstNameText =  (EditText) findViewById(R.id.editFirstname);
        final EditText lastNameText =  (EditText) findViewById(R.id.editLastname);
        final EditText carMakeText =  (EditText) findViewById(R.id.editCarMake);
        final EditText carRegText =  (EditText) findViewById(R.id.editCarReg);
        final EditText carSeatsText =  (EditText) findViewById(R.id.editCarSeats);
        final EditText priceText =  (EditText) findViewById(R.id.editPrice);

        Map<String, Object> userDetails = new HashMap<>();

        if(!TextUtils.isEmpty(firstNameText.getText().toString())){
            userDetails.put("firstname", firstNameText.getText().toString());
        }
        if(!TextUtils.isEmpty(lastNameText.getText().toString())){
            userDetails.put("lastname", lastNameText.getText().toString());
        }
        if(!TextUtils.isEmpty(carMakeText.getText().toString())){
            userDetails.put("car-make", carMakeText.getText().toString());
        }
        if(!TextUtils.isEmpty(carRegText.getText().toString())){
            userDetails.put("registration-no", carRegText.getText().toString());
        }
        if(!TextUtils.isEmpty(carSeatsText.getText().toString())){
            int n = Integer.parseInt(carSeatsText.getText().toString());
            userDetails.put("seats-no", n);
        }
        if(!TextUtils.isEmpty(priceText.getText().toString())){
            userDetails.put("ride-price", priceText.getText().toString());
        }
        if(latToServer != null){
            userDetails.put("latitude", latToServer);
            userDetails.put("longitude", lngToServer);
        }

        dataStore.collection("users").document(uid)
                .update(userDetails)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        Toast.makeText(EditProfileActivity.this, "Profile changes saved.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception ex) {
                        Toast.makeText(EditProfileActivity.this, "Could not save profile changes. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
