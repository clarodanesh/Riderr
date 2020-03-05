package com.riderrapp.riderr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.content.DialogInterface;
import android.location.Location;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.snackbar.SnackbarContentLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback;
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener;
import com.mapbox.services.android.navigation.ui.v5.listeners.RouteListener;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NavigationActivity extends AppCompatActivity implements OnNavigationReadyCallback,
        NavigationListener, RouteListener, ProgressChangeListener {

    private static final String TAG = "NavigationActivity";
    private NavigationView navigationView;
    private boolean dropoffDialogShown;
    private Location lastKnownLocation;
    private LocationListeningCallback callback = new LocationListeningCallback(this);
    private LocationEngine locationEngine;
    public static final String RIDE_ID = "rideId";
    private List<String> arr;
    private Map<String, Object> cData = new HashMap<>();
    private Map<String, String> uData = new HashMap<>();
    private Map<String, Long> lData = new HashMap<>();

    private List<Point> points = new ArrayList<>();

    String rideid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_AppCompat_NoActionBar);
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        //points.add(Point.fromLngLat(-2.477901, 53.755266));
        //points.add(Point.fromLngLat(-2.478621, 53.754781));
        //points.add(Point.fromLngLat(-2.479508, 53.754138));
        //points.add(Point.fromLngLat(-2.480254, 53.753245));
        //points.add(Point.fromLngLat(-2.480653, 53.752739));
        setContentView(R.layout.activity_navigation);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = fbuser.getUid();

        rideid = (String)getIntent().getExtras().get(RIDE_ID);

        DocumentReference docRef = db.collection("OfferedRides").document(rideid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        cData = document.getData();
                        Log.d(TAG, "Danesh");
                        List<Map> d = (List<Map>) cData.get("passengers");
                        for(int i = 0; i < d.size(); i++){
                            uData = d.get(i);

                            System.out.println("trophy " + uData.get("longitude"));
                            String s = uData.get("longitude");
                            String st = uData.get("latitude");
                            Double l = Double.parseDouble(s);
                            Double lt = Double.parseDouble(st);
                            Point p = Point.fromLngLat(l, lt);

                            points.add(Point.fromLngLat(l, lt));

                            System.out.println("tester" + p);

                        }

                        //after all the waypoints have been added, need to se the final destination of the
                        //whole journey here
                        points.add(Point.fromLngLat(document.getDouble("longitude"), document.getDouble("latitude")));

                        //navigation needs to be initialised
                        navigationView.initialize(NavigationActivity.this);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        locationEngine = LocationEngineProvider.getBestLocationEngine(this);
        locationEngine.getLastLocation(callback);

        navigationView = findViewById(R.id.navigationView);
        navigationView.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        navigationView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        navigationView.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        navigationView.onLowMemory();
    }

    @Override
    public void onBackPressed() {
        // If the navigation view didn't need to do anything, call super
        if (!navigationView.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        navigationView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        navigationView.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        navigationView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }

        navigationView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        navigationView.onDestroy();
    }

    @Override
    public void onNavigationReady(boolean isRunning) {
        fetchRoute(getLastKnownLocation(), points.remove(0));
    }

    @Override
    public void onCancelNavigation() {
        // Navigation canceled, finish the activity
        //showDidFinishDialog();
        showRatingDialog();
        //finish();
    }

    @Override
    public void onNavigationFinished() {
        // Intentionally empty
    }

    @Override
    public void onNavigationRunning() {
        // Intentionally empty
    }

    @Override
    public boolean allowRerouteFrom(Point offRoutePoint) {
        return true;
    }

    @Override
    public void onOffRoute(Point offRoutePoint) {

    }

    @Override
    public void onRerouteAlong(DirectionsRoute directionsRoute) {

    }

    @Override
    public void onFailedReroute(String errorMessage) {

    }

    @Override
    public void onArrival() {
        if (!dropoffDialogShown && !points.isEmpty()) {
            showDropoffDialog();
            dropoffDialogShown = true; // Accounts for multiple arrival events
            Toast.makeText(this, "You have arrived!", Toast.LENGTH_SHORT).show();
        }else if(!dropoffDialogShown && points.isEmpty()){
            //TODO show the last dialog and then stop navigation to stop calling on arrival again and again
            showLastDialog();
            stopNavigation();
        }
    }

    @Override
    public void onProgressChange(Location location, RouteProgress routeProgress) {
        lastKnownLocation = location;
    }

    private void startNavigation(DirectionsRoute directionsRoute) {
        NavigationViewOptions navigationViewOptions = setupOptions(directionsRoute);
        navigationView.startNavigation(navigationViewOptions);
    }

    private void stopNavigation() {
        navigationView.stopNavigation();
    }

    private void showDropoffDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.NavAlerts)).create();
        alertDialog.setMessage(getString(R.string.dropoff_dialog_text));
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,"YES",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int in) {
                fetchRoute(getLastKnownLocation(), points.remove(0));
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"NO",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int in) {
                finish();
            }
        });

        alertDialog.show();
    }

    private void showLastDialog() {
        //TODO update ride status here to completed, so that every user that loads again on the ride has to set a rating for therid
        AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.NavAlerts)).create();
        alertDialog.setMessage("The ride has come to an end");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"FINISH RIDE",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int in) {
                finish();
            }
        });

        alertDialog.show();
    }

    private void showDidFinishDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.NavAlerts)).create();
        alertDialog.setMessage("Did the ride come to an end? (Clicking yes will remove access to this ride.)");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,"YES",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int in) {
                //finish();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"NO",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int in) {
                finish();
            }
        });
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void showRatingDialog() {
        final EditText input = new EditText(this);

        AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.NavAlerts)).create();
        alertDialog.setMessage("Rate your passengers (0-5)");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,"SUBMIT RATING",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int in) {
                //finish();
                String ratingAsString;
                final int ratingAsInt;
                ratingAsString = input.getText().toString();
                ratingAsInt = Integer.parseInt(ratingAsString);
                if(ratingAsInt >= 0 && ratingAsInt <= 5){
                    final FirebaseFirestore db = FirebaseFirestore.getInstance();
                    FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
                    final String uid = fbuser.getUid();

                    DocumentReference docRef = db.collection("OfferedRides").document(rideid);
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    cData = document.getData();
                                    Log.d(TAG, "Danesh");
                                    List<Map> d = (List<Map>) cData.get("passengers");
                                    for(int i = 0; i < d.size(); i++){
                                        uData = d.get(i);
                                        lData = d.get(i);
                                        final String userId = uData.get("passenger");
                                        long rating = lData.get("rating");
                                        long amtOfRatings = lData.get("amountOfRatings");
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

                                        DocumentReference docRef = db.collection("users").document(userId);
                                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();
                                                    if (document.exists()) {
                                                        Map<String, Object> user = new HashMap<>();
                                                        user.put("rating", ratingToAddToDB);
                                                        user.put("amountOfRatings", amtOfRatingsIncludingThis);

                                                        db.collection("users").document(userId)
                                                                .update(user)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        Log.d(TAG, "USER RATINGS UPDATED");
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.w(TAG, "Error writing document", e);
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

                                    }

                                    //after all the waypoints have been added, need to se the final destination of the
                                    //whole journey here
                                    points.add(Point.fromLngLat(document.getDouble("longitude"), document.getDouble("latitude")));

                                    //navigation needs to be initialised
                                    navigationView.initialize(NavigationActivity.this);
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
                }else{
                    Snackbar ratingSB = Snackbar.make(navigationView, "You need to enter a number on the scale 0-5", Snackbar.LENGTH_LONG);
                    ratingSB.getView().setBackgroundColor(ContextCompat.getColor(NavigationActivity.this, R.color.colorAccent));
                    View view = ratingSB.getView();
                    TextView tv = (TextView) view.findViewById((com.google.android.material.R.id.snackbar_text));
                    tv.setTextColor(ContextCompat.getColor(NavigationActivity.this, R.color.colorPrimary));
                    ratingSB.show();
                }
            }
        });


        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        ColorStateList colorStateList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent));
        ViewCompat.setBackgroundTintList(input, colorStateList);
        input.setTextColor(colorStateList);

        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private void fetchRoute(Point origin, Point destination) {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .alternatives(true)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<DirectionsResponse> call, @NotNull Response<DirectionsResponse> response) {
                        DirectionsResponse directionsResponse = response.body();
                        if (directionsResponse != null && !directionsResponse.routes().isEmpty()) {
                            startNavigation(directionsResponse.routes().get(0));
                        }
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                });
    }

    private NavigationViewOptions setupOptions(DirectionsRoute directionsRoute) {
        dropoffDialogShown = false;

        NavigationViewOptions.Builder options = NavigationViewOptions.builder();
        options.directionsRoute(directionsRoute)
                .navigationListener(this)
                .progressChangeListener(this)
                .routeListener(this)
                .shouldSimulateRoute(true);
        return options.build();
    }

    private Point getLastKnownLocation() {
        return Point.fromLngLat(callback.lastLocation.getLongitude(), callback.lastLocation.getLatitude());
    }

    private static class LocationListeningCallback
            implements LocationEngineCallback<LocationEngineResult> {

        //private final WeakReference<MainActivity> activityWeakReference;
        public Location lastLocation;

        LocationListeningCallback(NavigationActivity activity) {
            //this.activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            // The LocationEngineCallback interface's method which fires when the device's location has changed.
            lastLocation = result.getLastLocation();
        }

        @Override
        public void onFailure(@NonNull Exception exception) {

            // The LocationEngineCallback interface's method which fires when the device's location can not be captured

        }
    }
}
