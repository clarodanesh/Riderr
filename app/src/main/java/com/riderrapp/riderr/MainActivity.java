package com.riderrapp.riderr;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private MapView mainActivityMap;
    private SymbolManager mapMarkerManager;
    private Symbol mapMarkerSymbol;
    private static final String MARKER_ID = "marker";
    private double lat = 0, lng = 0;
    private String pride = "", dride = "";
    private boolean rideComplete;

    private Map<String, Object> rideDataObjectsMap = new HashMap<>();
    private Map<String, String> passengerDataStringMap = new HashMap<>();
    private Map<String, Long> passengerDataLongMap = new HashMap<>();

    final FirebaseFirestore dataStore = FirebaseFirestore.getInstance();
    final FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle instanceState) {
        super.onCreate(instanceState);
        Mapbox.getInstance(this, "pk.eyJ1IjoiZGlxYmFsIiwiYSI6ImNqdzZtMzQ3czAxZXYzem83eTY4NWpua2kifQ.clrFPQXs0E70rz9R4H292w");
        setContentView(R.layout.activity_main);

        Toolbar mainActivityToolbar = findViewById(R.id.mainActivityToolbar);
        setSupportActionBar(mainActivityToolbar);

        DrawerLayout navDrawer = findViewById(R.id.mainActivityNavDrawerLayout);
        NavigationView navDrawerView = findViewById(R.id.navDrawerView);
        ActionBarDrawerToggle navDrawerToggler = new ActionBarDrawerToggle(this, navDrawer, mainActivityToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        navDrawer.addDrawerListener(navDrawerToggler);
        navDrawerToggler.syncState();
        navDrawerView.setNavigationItemSelectedListener(this);

        final Intent searchIntent = new Intent(this, SearchActivity.class);
        Button searchButton = (Button) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(searchIntent);
            }
        });

        final Intent offerRideIntent = new Intent(this, OfferRideActivity.class);
        Button offerRideButton = (Button) findViewById(R.id.offerRideButton);
        offerRideButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(offerRideIntent);
            }
        });

        mainActivityMap = findViewById(R.id.activeRideMapView);
        mainActivityMap.onCreate(instanceState);
        mainActivityMap.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap readyMap) {
                readyMap.getUiSettings().setAllGesturesEnabled(false);
                readyMap.setStyle(new Style.Builder().fromUri("mapbox://styles/diqbal/ck743r90t2pt01io833c6fdpg"), new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull final Style loadedStyle) {
                        String uid = currUser.getUid();

                        DocumentReference usersReference = dataStore.collection("users").document(uid);
                        usersReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> returnedTask) {
                                if (returnedTask.isSuccessful()) {
                                    DocumentSnapshot userDocument = returnedTask.getResult();
                                    if (userDocument.exists()) {
                                        pride = userDocument.getString("p-ride");
                                        dride = userDocument.getString("d-ride");

                                        if(dride != null) {
                                            SetRide(loadedStyle, readyMap, dride, "driver");
                                        }else if(pride != null){
                                            SetRide(loadedStyle, readyMap, pride, "passenger");
                                        }else{
                                            Log.d(TAG, "Carry on no rides !!!");
                                        }
                                    } else {
                                        Log.d(TAG, "No ride found");
                                    }
                                } else {
                                    Log.d(TAG, "Exception: ", returnedTask.getException());
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem navItem) {
        int navItemId = navItem.getItemId();

        if (navItemId == R.id.nav_far) {
            final Intent searchIntent = new Intent(this, SearchActivity.class);
            startActivity(searchIntent);
            return true;
        } else if (navItemId == R.id.nav_oar) {
            final Intent offerRideIntent = new Intent(this, OfferRideActivity.class);
            startActivity(offerRideIntent);
            return true;
        } else if (navItemId == R.id.nav_vr) {
            final Intent viewRidesIntent = new Intent(this, ViewRidesActivity.class);
            startActivity(viewRidesIntent);
            return true;
        } else if (navItemId == R.id.nav_profile) {
            final Intent profileIntent = new Intent(this, ProfileActivity.class);
            startActivity(profileIntent);
            return true;
        } else if (navItemId == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;
        }

        DrawerLayout navDrawer = findViewById(R.id.mainActivityNavDrawerLayout);
        navDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void SetRide(final Style loadedStyle, final MapboxMap readyMap, final String rideid, final String rideType){
        DocumentReference rideRef = dataStore.collection("OfferedRides").document(rideid);
        rideRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> returnedTask) {
                if (returnedTask.isSuccessful()) {
                    DocumentSnapshot rideDoc = returnedTask.getResult();
                    if (rideDoc.exists()) {
                        lat = rideDoc.getDouble("latitude");
                        lng = rideDoc.getDouble("longitude");
                        rideComplete = rideDoc.getBoolean("completed");

                        if(rideComplete && rideType.equals("driver")){
                            showDriverRatingDialog(rideid, rideType);
                        }else if(rideComplete && rideType.equals("passenger")){
                            showPassRatingDialog(rideid, rideType);
                        }else {
                            InsertMarkerToStyle(loadedStyle);

                            // create symbol manager
                            GeoJsonOptions gjOptions = new GeoJsonOptions().withTolerance(0.4f);
                            mapMarkerManager = new SymbolManager(mainActivityMap, readyMap, loadedStyle, null, gjOptions);
                            mapMarkerManager.deleteAll();

                            mapMarkerManager = new SymbolManager(mainActivityMap, readyMap, loadedStyle, null, gjOptions);
                            // set non data driven properties
                            mapMarkerManager.setIconAllowOverlap(true);
                            mapMarkerManager.setTextAllowOverlap(true);

                            // create a symbol
                            SymbolOptions mapMarkerSymbolOptions = new SymbolOptions()
                                    .withLatLng(new LatLng(lat, lng))
                                    .withSymbolSortKey(10.0f)
                                    .withIconImage(MARKER_ID)
                                    .withIconSize(1f);

                            mapMarkerSymbol = mapMarkerManager.create(mapMarkerSymbolOptions);

                            readyMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                    new CameraPosition.Builder()
                                            .target(new LatLng(lat, lng))
                                            .zoom(8)
                                            .build()), 500);
                        }
                    } else {
                        ShowRideCancelledDialog();
                    }
                } else {
                    Log.d(TAG, "Exception: ", returnedTask.getException());
                }
            }
        });
    }

    private void InsertMarkerToStyle(Style loadedStyle) {
        loadedStyle.addImage(MARKER_ID, BitmapUtils.getBitmapFromDrawable(ContextCompat.getDrawable(this, R.drawable.map_marker_dark)));
    }

    @Override
    public void onStart() {
        super.onStart();
        mainActivityMap.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivityMap.onResume();

        mainActivityMap.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap readyMap) {
                readyMap.getUiSettings().setAllGesturesEnabled(false);
                readyMap.setStyle(new Style.Builder().fromUri("mapbox://styles/diqbal/ck743r90t2pt01io833c6fdpg"), new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull final Style loadedStyle) {
                        String uid = currUser.getUid();

                        DocumentReference usersReference = dataStore.collection("users").document(uid);
                        usersReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> returnedTask) {
                                if (returnedTask.isSuccessful()) {
                                    DocumentSnapshot userDoc = returnedTask.getResult();
                                    if (userDoc.exists()) {
                                        pride = userDoc.getString("p-ride");
                                        dride = userDoc.getString("d-ride");

                                        if(dride != null) {
                                            SetRide(loadedStyle, readyMap, dride, "driver");
                                        }else if(pride != null){
                                            SetRide(loadedStyle, readyMap, pride, "passenger");
                                        }else{
                                            Log.d(TAG, "Carry on no rides !!!");
                                        }
                                    } else {
                                        Log.d(TAG, "Ride doesnt exist :: onResum check");
                                    }
                                } else {
                                    Log.d(TAG, "Exception ", returnedTask.getException());
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mainActivityMap.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mainActivityMap.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mainActivityMap.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainActivityMap.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle offState) {
        super.onSaveInstanceState(offState);
        mainActivityMap.onSaveInstanceState(offState);
    }

    private void DeleteRideFromUser(String rideType){
        FirebaseUser signedInUser = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> userMap = new HashMap<>();
        if(rideType.equals("driver")){
            userMap.put("d-ride", null);
        }else{
            userMap.put("p-ride", null);
        }

        dataStore.collection("users").document(signedInUser.getUid())
                .update(userMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "USER RATINGS UPDATED");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Failed update DeleteRideFromUser();", e);
                    }
                });
    }

    private void ShowRideCancelledDialog(){
        AlertDialog rideCancelledDialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.NavAlerts)).create();
        rideCancelledDialog.setMessage("Your previous ride was cancelled");
        rideCancelledDialog.setButton(AlertDialog.BUTTON_POSITIVE,"OK",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dInterface, int num) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("p-ride", null);
                dataStore.collection("users").document(currUser.getUid())
                        .update(userMap)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                Toast.makeText(MainActivity.this, "Ride removed.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Failed update ShowRideCancelledDialog();", e);
                            }
                        });
            }
        });

        rideCancelledDialog.setCancelable(false);
        rideCancelledDialog.show();
    }

    private void showDriverRatingDialog(final String rid, final String rideType) {
        final EditText driverRatingInput = new EditText(this);

        final AlertDialog driverRatingDialog = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.NavAlerts)).create();
        driverRatingDialog.setMessage("Rate your Passengers (0-5)");
        driverRatingDialog.setButton(AlertDialog.BUTTON_POSITIVE,"SUBMIT RATING", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dInterface, int num) {
                String ratingAsString;
                final int ratingAsInt;
                ratingAsString = driverRatingInput.getText().toString();
                ratingAsInt = Integer.parseInt(ratingAsString);
                if(ratingAsInt >= 0 && ratingAsInt <= 5){
                    DocumentReference rideRef = dataStore.collection("OfferedRides").document(rid);
                    rideRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> returnedTask) {
                            if (returnedTask.isSuccessful()) {
                                DocumentSnapshot rideDoc = returnedTask.getResult();
                                if (rideDoc.exists()) {
                                    rideDataObjectsMap = rideDoc.getData();
                                    List<Map> d = (List<Map>) rideDataObjectsMap.get("passengers");
                                    for(int i = 0; i < d.size(); i++){
                                        passengerDataStringMap = d.get(i);
                                        passengerDataLongMap = d.get(i);
                                        final String userId = passengerDataStringMap.get("passenger");
                                        long rating = passengerDataLongMap.get("rating");
                                        long amtOfRatings = passengerDataLongMap.get("amountOfRatings");
                                        final long amtOfRatingsIncludingThis = amtOfRatings + 1;
                                        final long ratingToAddToDB;
                                        final float ratingBeforeRounding;
                                        final long accumulatedRating;

                                        if(rating == -1){
                                            ratingToAddToDB = ratingAsInt / 1;
                                        }else{
                                            accumulatedRating = amtOfRatings * rating;
                                            ratingBeforeRounding = ((float)ratingAsInt + (float)accumulatedRating) / (float)amtOfRatingsIncludingThis;
                                            ratingToAddToDB = Math.round(ratingBeforeRounding);
                                        }

                                        DocumentReference usersReference = dataStore.collection("users").document(userId);
                                        usersReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> returnedTask) {
                                                if (returnedTask.isSuccessful()) {
                                                    DocumentSnapshot userDoc = returnedTask.getResult();
                                                    if (userDoc.exists()) {
                                                        Map<String, Object> user = new HashMap<>();
                                                        user.put("rating", ratingToAddToDB);
                                                        user.put("amountOfRatings", amtOfRatingsIncludingThis);

                                                        dataStore.collection("users").document(userId)
                                                                .update(user)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void v) {
                                                                        Log.d(TAG, "USER RATINGS UPDATED");
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.w(TAG, "Failed update driverRatingDialog userRef", e);
                                                                    }
                                                                });
                                                    } else {
                                                        Log.d(TAG, "Could get user doc");
                                                    }
                                                } else {
                                                    Log.d(TAG, "Exception: ", returnedTask.getException());
                                                }
                                            }
                                        });

                                    }
                                    DeleteRideFromUser(rideType);
                                } else {
                                    Log.d(TAG, "Couldnt find doc");
                                }
                            } else {
                                Log.d(TAG, "Exception: ", returnedTask.getException());
                            }
                        }
                    });
                }else{
                    driverRatingDialog.dismiss();
                    AlertDialog errorMessageDialog = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.NavAlerts)).create();
                    errorMessageDialog.setMessage("Enter a number from 0-5");
                    errorMessageDialog.setButton(AlertDialog.BUTTON_POSITIVE,"OK",new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dInterface, int num) {
                            showDriverRatingDialog(rid, rideType);
                        }
                    });

                    errorMessageDialog.setCancelable(false);
                    errorMessageDialog.show();
                }
            }
        });


        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        driverRatingInput.setLayoutParams(lp);
        driverRatingDialog.setView(driverRatingInput);

        ColorStateList csList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent));
        ViewCompat.setBackgroundTintList(driverRatingInput, csList);
        driverRatingInput.setTextColor(csList);

        driverRatingDialog.setCancelable(false);
        driverRatingDialog.show();
    }

    private void showPassRatingDialog(final String rid, final String rideType) {
        final EditText passRatingInput = new EditText(this);

        final AlertDialog passRatingDialog = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.NavAlerts)).create();
        passRatingDialog.setMessage("Rate your Driver (0-5)");
        passRatingDialog.setButton(AlertDialog.BUTTON_POSITIVE,"SUBMIT RATING",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int in) {
                String ratingAsString;
                final int ratingAsInt;
                ratingAsString = passRatingInput.getText().toString();
                ratingAsInt = Integer.parseInt(ratingAsString);
                if(ratingAsInt >= 0 && ratingAsInt <= 5){
                    DocumentReference rideRef = dataStore.collection("OfferedRides").document(rid);
                    rideRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot rideDoc = task.getResult();
                                if (rideDoc.exists()) {
                                    DocumentReference driverDocRef = dataStore.collection("users").document(rideDoc.getString("offeredBy"));
                                    driverDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot userDoc = task.getResult();
                                                if (userDoc.exists()) {
                                                    UpdateRating(userDoc.getString("user-id"), userDoc.getLong("rating"), userDoc.getLong("amountOfRatings"), ratingAsInt, rideType);
                                                } else {
                                                    Log.d(TAG, "No such document");
                                                }
                                            } else {
                                                Log.d(TAG, "get failed with ", task.getException());
                                            }
                                        }
                                    });
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
                }else{
                    passRatingDialog.dismiss();
                    AlertDialog errorMessageDialog = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.NavAlerts)).create();
                    errorMessageDialog.setMessage("Enter a number from 0-5");
                    errorMessageDialog.setButton(AlertDialog.BUTTON_POSITIVE,"OK",new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dInterface, int num) {
                            showPassRatingDialog(rid, rideType);
                        }
                    });

                    errorMessageDialog.setCancelable(false);
                    errorMessageDialog.show();
                }
            }
        });


        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        passRatingInput.setLayoutParams(lp);
        passRatingDialog.setView(passRatingInput);

        ColorStateList csList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent));
        ViewCompat.setBackgroundTintList(passRatingInput, csList);
        passRatingInput.setTextColor(csList);

        passRatingDialog.setCancelable(false);
        passRatingDialog.show();
    }

    private void UpdateRating(final String userId, long rating, long amtOfRatings, int ratingAsInt, final String rideType){
        final long amtOfRatingsIncludingThis = amtOfRatings + 1;
        final long ratingToAddToDB;
        final float ratingBeforeRounding;
        final long accumulatedRating;

        if(rating == -1){
            ratingToAddToDB = ratingAsInt / 1;
        }else{
            accumulatedRating = amtOfRatings * rating;
            ratingBeforeRounding = ((float)ratingAsInt + (float)accumulatedRating) / (float)amtOfRatingsIncludingThis;
            ratingToAddToDB = Math.round(ratingBeforeRounding);
        }

        DocumentReference userRef = dataStore.collection("users").document(userId);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> returnedTask) {
                if (returnedTask.isSuccessful()) {
                    DocumentSnapshot userDoc = returnedTask.getResult();
                    if (userDoc.exists()) {
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("rating", ratingToAddToDB);
                        userMap.put("amountOfRatings", amtOfRatingsIncludingThis);

                        dataStore.collection("users").document(userId)
                                .update(userMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void v) {
                                        Log.d(TAG, "USER RATINGS UPDATED");
                                        DeleteRideFromUser(rideType);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Failed to update in UpdateRating();", e);
                                    }
                                });
                    } else {
                        Log.d(TAG, "Couldnt find the user");
                    }
                } else {
                    Log.d(TAG, "Exception: ", returnedTask.getException());
                }
            }
        });
    }
}
