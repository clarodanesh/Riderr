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

    //Member variables for the Edit profile activity
    private static final String TAG = "EditProfileActivity";
    private String latToServer = null, lngToServer = null;
    private LocationManager locManager;
    FirebaseFirestore dataStore = FirebaseFirestore.getInstance();
    FirebaseUser authUser = FirebaseAuth.getInstance().getCurrentUser();
    String uid = authUser.getUid();

    //Need to create a location listener, listens for the onLocationChanged event
    //will be used for when the user updates their location
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

    //Result callback for the location permission result.
    //Iam checking if the location perm is granted if not then allow user to carry on but not update loc
    //if it is granted then update the location variables ready to send to server
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

    //AppCompatActivity allows overriding an onCreate method which is called when create even is triggered by AppcompatActivity
    @Override
    protected void onCreate(Bundle instanceState) {
        super.onCreate(instanceState);

        //need to get the layout I made for edit profile and set it
        setContentView(R.layout.activity_edit_profile);

        //need to get the views created for the save profile and update loc buttons into Button objects
        final Button saveProfileChangesBtn = (Button) findViewById(R.id.SaveProfileChangesBtn);
        final Button updateLocationBtn = (Button) findViewById(R.id.updateLocationBtn);

        //click listener for update loc button
        updateLocationBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //when the button has been clicked need to check if the perm is granted for loc if not then ask for it
                if (ActivityCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(EditProfileActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    return;
                }else{
                    //loc perm was granted so set the variables to send to server
                    locManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                    locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100,
                            1, locListener);

                    Location loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    latToServer = Double.toString(loc.getLatitude());
                    lngToServer = Double.toString(loc.getLongitude());
                }
            }
        });

        //click listener for save profile changes button
        //just need to save the changes to the server when clicked and close this activity
        saveProfileChangesBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SaveProfileChanges();
                finish();
            }
        });
    }

    //on start method overriden as it is provided by appcompatactivity
    @Override
    public void onStart() {
        super.onStart();

        //populate the details into the edit text views
        PopulateDetails();

        //check for the loc permissions first and if not granted then ask for them
        if (ActivityCompat.checkSelfPermission(EditProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditProfileActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }else{
            //use the location mananger to get the location
            locManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 1, locListener);
        }
    }

    //this method will set textviews to enabled or disabled for the names fields
    //first time users who have default names can change their name
    //if they are not first time user cannot change name for fraud protection
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

    //this method populates the edittext views with details fetched from the db stored on the server
    private void PopulateDetails(){
        dataStore = FirebaseFirestore.getInstance();
        authUser = FirebaseAuth.getInstance().getCurrentUser();
        uid = authUser.getUid();

        //get the document in the db which is stored under this users uid
        DocumentReference usersReference = dataStore.collection("users").document(uid);
        usersReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> returnedTask) {
                if (returnedTask.isSuccessful()) {
                    DocumentSnapshot usersDocument = returnedTask.getResult();
                    if (usersDocument.exists()) {
                        //if the user document exists in db then need to populate edittexts with returned values
                        final EditText firstNameEdit =  (EditText) findViewById(R.id.editFirstname);
                        final EditText lastNameEdit =  (EditText) findViewById(R.id.editLastname);
                        final EditText carMakeEdit =  (EditText) findViewById(R.id.editCarMake);
                        final EditText carRegEdit =  (EditText) findViewById(R.id.editCarReg);
                        final EditText carSeatsEdit =  (EditText) findViewById(R.id.editCarSeats);
                        final EditText priceEdit =  (EditText) findViewById(R.id.editPrice);

                        //setting the hints as opposed to text so that the user doesnt have to
                        //remove text in order to enter details
                        firstNameEdit.setHint(usersDocument.getString("firstname"));
                        lastNameEdit.setHint(usersDocument.getString("lastname"));
                        carMakeEdit.setHint(usersDocument.getString("car-make"));
                        carRegEdit.setHint(usersDocument.getString("registration-no"));
                        priceEdit.setHint(usersDocument.getString("ride-price"));

                        int seatsNo = usersDocument.getLong("seats-no").intValue();
                        String seatsNoString = String.valueOf(seatsNo);
                        carSeatsEdit.setHint(seatsNoString);

                        //when the details return check this is first time user so appropriate fields are available to change
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

    //User has clicked save profile, so can now send details to the server, first need to check what has been filled
    private void SaveProfileChanges(){
        //get firebaseFirestore instance and then the currUser
        dataStore = FirebaseFirestore.getInstance();
        authUser = FirebaseAuth.getInstance().getCurrentUser();
        uid = authUser.getUid();

        //set all the edit text views into edit text objects so we can use their methods
        final EditText firstNameText =  (EditText) findViewById(R.id.editFirstname);
        final EditText lastNameText =  (EditText) findViewById(R.id.editLastname);
        final EditText carMakeText =  (EditText) findViewById(R.id.editCarMake);
        final EditText carRegText =  (EditText) findViewById(R.id.editCarReg);
        final EditText carSeatsText =  (EditText) findViewById(R.id.editCarSeats);
        final EditText priceText =  (EditText) findViewById(R.id.editPrice);

        Map<String, Object> userDetails = new HashMap<>();

        //need to check whatever has been filled and then send those details to the server
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

        //update the document in the db using firebase update method
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
