package com.riderrapp.riderr;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.DialogInterface;
import android.location.Location;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback;
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener;
import com.mapbox.services.android.navigation.ui.v5.listeners.RouteListener;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NavigationActivity extends AppCompatActivity implements OnNavigationReadyCallback, NavigationListener, RouteListener, ProgressChangeListener {

    private static final String TAG = "NavigationActivity";
    private NavigationView rideNavView;
    private boolean dialogOpen;
    private locListenCallback locListeningCallback = new locListenCallback(this);
    private LocationEngine locEng;
    public static final String RIDE_ID = "rideId";
    private Map<String, Object> rideDocumentData = new HashMap<>();
    private Map<String, String> userData = new HashMap<>();

    private List<Point> routePoints = new ArrayList<>();

    private String rideid;

    final FirebaseFirestore dataStore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle instanceState) {
        setTheme(R.style.Theme_AppCompat_NoActionBar);
        super.onCreate(instanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_navigation);

        rideid = (String)getIntent().getExtras().get(RIDE_ID);

        DocumentReference docRef = dataStore.collection("OfferedRides").document(rideid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> returnedTask) {
                if (returnedTask.isSuccessful()) {
                    DocumentSnapshot rideDoc = returnedTask.getResult();
                    if (rideDoc.exists()) {
                        rideDocumentData = rideDoc.getData();
                        List<Map> d = (List<Map>) rideDocumentData.get("passengers");
                        for(int i = 0; i < d.size(); i++){
                            userData = d.get(i);

                            String lngString = userData.get("longitude");
                            String latString = userData.get("latitude");
                            Double l = Double.parseDouble(lngString);
                            Double lt = Double.parseDouble(latString);

                            routePoints.add(Point.fromLngLat(l, lt));
                        }

                        routePoints.add(Point.fromLngLat(rideDoc.getDouble("longitude"), rideDoc.getDouble("latitude")));

                        //navigation needs to be initialised
                        rideNavView.initialize(NavigationActivity.this);
                    } else {
                        Log.d(TAG, "Cant get ride data");
                    }
                } else {
                    Log.d(TAG, "Exception: ", returnedTask.getException());
                }
            }
        });

        locEng = LocationEngineProvider.getBestLocationEngine(this);
        locEng.getLastLocation(locListeningCallback);

        rideNavView = findViewById(R.id.rideNavView);
        rideNavView.onCreate(instanceState);

    }

    @Override
    public void onStart() {
        super.onStart();
        rideNavView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        rideNavView.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        rideNavView.onLowMemory();
    }

    @Override
    public void onBackPressed() {
        if (!rideNavView.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        rideNavView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        rideNavView.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        rideNavView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (locEng != null) {
            locEng.removeLocationUpdates(locListeningCallback);
        }
        rideNavView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rideNavView.onDestroy();
    }

    @Override
    public void onNavigationReady(boolean isRunning) {
        MakeRoute(GetLatestLocation(), routePoints.remove(0));
    }

    @Override
    public void onFailedReroute(String errorMessage) {

    }

    @Override
    public void onNavigationFinished() {

    }

    @Override
    public void onCancelNavigation() {
        finish();
    }

    @Override
    public void onOffRoute(Point offRoutePoint) {

    }

    @Override
    public void onRerouteAlong(DirectionsRoute directionsRoute) {

    }

    @Override
    public void onNavigationRunning() {

    }

    @Override
    public boolean allowRerouteFrom(Point offRoutePoint) {
        return true;
    }

    @Override
    public void onProgressChange(Location location, RouteProgress routeProgress) {

    }

    @Override
    public void onArrival() {
        if (!dialogOpen && !routePoints.isEmpty()) {
            OpenArrivalDialog();
            dialogOpen = true;
            Toast.makeText(this, "You have arrived!", Toast.LENGTH_SHORT).show();
        }else if(!dialogOpen && routePoints.isEmpty()){
            ShowLastDialog();
            SetRideToCompleted();
            StopNavView();
        }
    }

    private void StartNavView(DirectionsRoute directionsRoute) {
        NavigationViewOptions navigationViewOptions = setupOptions(directionsRoute);
        rideNavView.startNavigation(navigationViewOptions);
    }

    private void StopNavView() {
        rideNavView.stopNavigation();
    }

    private void OpenArrivalDialog() {
        AlertDialog arrivalDialog = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.NavAlerts)).create();
        arrivalDialog.setMessage("Navigate to next destination?");
        arrivalDialog.setButton(AlertDialog.BUTTON_POSITIVE,"YES",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dInterface, int num) {
                MakeRoute(GetLatestLocation(), routePoints.remove(0));
            }
        });
        arrivalDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"NO",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dInterface, int num) {
                finish();
            }
        });

        arrivalDialog.show();
    }

    private void ShowLastDialog() {
        AlertDialog finalDialog = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.NavAlerts)).create();
        finalDialog.setMessage("The ride has come to an end");
        finalDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"FINISH RIDE",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dInterface, int num) {
                finish();
            }
        });

        finalDialog.show();
    }

    private void MakeRoute(Point startPoint, Point destPoint) {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(startPoint)
                .destination(destPoint)
                .alternatives(true)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(@NotNull Call<DirectionsResponse> directionCall, @NotNull Response<DirectionsResponse> directionResponse) {
                        DirectionsResponse dResponse = directionResponse.body();
                        if (dResponse != null && !dResponse.routes().isEmpty()) {
                            StartNavView(dResponse.routes().get(0));
                        }
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> directionCall, Throwable t) {
                        Log.e(TAG, "Thrown: " + t.getMessage());
                    }
                });
    }

    private void SetRideToCompleted(){
        Map<String, Object> rideMap = new HashMap<>();
        rideMap.put("completed", true);

        dataStore.collection("OfferedRides").document(rideid)
                .update(rideMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        Log.d(TAG, "USER RATINGS UPDATED");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Update failed in SetRideToCompleted();", e);
                    }
                });
    }

    private NavigationViewOptions setupOptions(DirectionsRoute dRoute) {
        dialogOpen = false;

        NavigationViewOptions.Builder navBuilderOptions = NavigationViewOptions.builder();
        navBuilderOptions.directionsRoute(dRoute)
                .navigationListener(this)
                .progressChangeListener(this)
                .routeListener(this)
                .shouldSimulateRoute(true);
        return navBuilderOptions.build();
    }

    private Point GetLatestLocation() {
        return Point.fromLngLat(locListeningCallback.lastLoc.getLongitude(), locListeningCallback.lastLoc.getLatitude());
    }

    private static class locListenCallback implements LocationEngineCallback<LocationEngineResult> {

        public Location lastLoc;

        locListenCallback(NavigationActivity activity) {

        }

        @Override
        public void onSuccess(LocationEngineResult res) {
            lastLoc = res.getLastLocation();
        }

        @Override
        public void onFailure(@NonNull Exception exception) {
            Log.d(TAG, "Exception: ", exception);
        }
    }
}
