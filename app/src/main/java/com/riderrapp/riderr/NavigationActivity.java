package com.riderrapp.riderr;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.content.DialogInterface;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

import java.util.ArrayList;
import java.util.List;

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

    private List<Point> points = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_AppCompat_NoActionBar);
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        points.add(Point.fromLngLat(-2.477901, 53.755266));
        //points.add(Point.fromLngLat(-2.478621, 53.754781));
        //points.add(Point.fromLngLat(-2.479508, 53.754138));
        //points.add(Point.fromLngLat(-2.480254, 53.753245));
        //points.add(Point.fromLngLat(-2.480653, 53.752739));
        setContentView(R.layout.activity_navigation);

        LocationEngine locationEngine = LocationEngineProvider.getBestLocationEngine(this);
        locationEngine.getLastLocation(callback);

        navigationView = findViewById(R.id.navigationView);
        navigationView.onCreate(savedInstanceState);
        navigationView.initialize(this);
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
        finish();
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
        }else if(points.isEmpty()){
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
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
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
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setMessage("The ride has come to an end");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"FINISH RIDE",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int in) {
                finish();
            }
        });

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
