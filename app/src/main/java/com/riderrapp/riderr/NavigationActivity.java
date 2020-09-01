package com.riderrapp.riderr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

    //member variables for the navigationactivity class
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
        //Mapbox docs hint that the nav activity should have no actionbar since the nav needs to take the whole screen
        setTheme(R.style.Theme_AppCompat_NoActionBar);
        super.onCreate(instanceState);
        //using the mapbox api again so need to get an instance using my access token
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_navigation);

        //i need to get the rideid from the view rides activity from the intent sent here
        rideid = (String)getIntent().getExtras().get(RIDE_ID);

        //use the rideid to get the data from the offeredRide
        DocumentReference docRef = dataStore.collection("OfferedRides").document(rideid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> returnedTask) {
                if (returnedTask.isSuccessful()) {
                    DocumentSnapshot rideDoc = returnedTask.getResult();
                    if (rideDoc.exists()) {
                        //need to get the ride data, only need lat and lng
                        //lat and lng needs to be put into the points to make nav route
                        rideDocumentData = rideDoc.getData();
                        List<Map> d = (List<Map>) rideDocumentData.get("passengers");
                        for(int i = 0; i < d.size(); i++){
                            userData = d.get(i);

                            String lngString = userData.get("longitude");
                            String latString = userData.get("latitude");
                            Double l = Double.parseDouble(lngString);
                            Double lt = Double.parseDouble(latString);

                            //add the points to the route points
                            routePoints.add(Point.fromLngLat(l, lt));
                        }

                        //add the last point to the route
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

        //need to get the last location from the location engine
        locEng = LocationEngineProvider.getBestLocationEngine(this);
        locEng.getLastLocation(locListeningCallback);

        //need to get the layout of the nav view
        rideNavView = findViewById(R.id.rideNavView);
        //need to handle the oncreate for the nav view
        rideNavView.onCreate(instanceState);
    }

    //Need to override onstart for the navview
    @Override
    public void onStart() {
        super.onStart();
        rideNavView.onStart();
    }

    //Need to override onresume for the navview
    @Override
    public void onResume() {
        super.onResume();
        rideNavView.onResume();
    }

    //Need to override onlowmemory for the navview
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        rideNavView.onLowMemory();
    }

    //Need to override onbackpressed for the navview
    @Override
    public void onBackPressed() {
        if (!rideNavView.onBackPressed()) {
            super.onBackPressed();
        }
    }

    //Need to override onsavedinstancestate for the navview
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        rideNavView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    //Need to override onrestoredinstancestate for the navview
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        rideNavView.onRestoreInstanceState(savedInstanceState);
    }

    //Need to override onpause for the navview
    @Override
    public void onPause() {
        super.onPause();
        rideNavView.onPause();
    }

    //Need to override onstop for the navview
    //Need to remove the location updates here to stop memory leaks
    @Override
    public void onStop() {
        super.onStop();
        if (locEng != null) {
            locEng.removeLocationUpdates(locListeningCallback);
        }
        rideNavView.onStop();
    }

    //Need to override ondestroy for the navview
    @Override
    protected void onDestroy() {
        super.onDestroy();
        rideNavView.onDestroy();
    }

    //Need to override onnavigationready for the navview
    //need to start the route with current location and the first point in the route
    @Override
    public void onNavigationReady(boolean isRunning) {
        MakeRoute(GetLatestLocation(), routePoints.remove(0));
    }

    @Override
    public void onFailedReroute(String errorMessage) {
        //need to override the failed reroute method because using the route listener
        //not implementing it though
    }

    @Override
    public void onNavigationFinished() {
        //need to override the nav finished method because using the route listener
        //not implementing it though
    }

    //when the user cancels the nav then call finish to close the activity
    @Override
    public void onCancelNavigation() {
        finish();
    }

    @Override
    public void onOffRoute(Point offRoutePoint) {
        //need to override the offroute method because using the route listener
        //not implementing it though
    }

    @Override
    public void onRerouteAlong(DirectionsRoute directionsRoute) {
        //need to override the reroute along method because using the route listener
        //not implementing it though
    }

    @Override
    public void onNavigationRunning() {
        //need to override the navrunning method because using the route listener
        //not implementing it though
    }

    //check if the nav should be allowed to reroute from the offroute point if so then just return true
    @Override
    public boolean allowRerouteFrom(Point offRoutePoint) {
        return true;
    }

    @Override
    public void onProgressChange(Location location, RouteProgress routeProgress) {
        //need to override the on progress change method because using the route listener
        //not implementing it though
    }

    //triggers when the user reaches a point in the route
    @Override
    public void onArrival() {
        //if the dialog is not shown and havent reached all route points
        //need to show the you have arrived dialog
        if (!dialogOpen && !routePoints.isEmpty()) {
            OpenArrivalDialog();
            dialogOpen = true;
            Toast.makeText(this, "You have arrived!", Toast.LENGTH_SHORT).show();
        }else if(!dialogOpen && routePoints.isEmpty()){
            //if the user has finished the ride and dialog isnt open then show the last dialog
            ShowLastDialog();
            SetRideToCompleted();
            StopNavView();
        }
    }

    //startNavView will start the navigation with the navviewoptions
    //and the directions route
    private void StartNavView(DirectionsRoute directionsRoute) {
        NavigationViewOptions navigationViewOptions = setupOptions(directionsRoute);
        rideNavView.startNavigation(navigationViewOptions);
    }

    //stop the navigation view
    private void StopNavView() {
        rideNavView.stopNavigation();
    }

    //this method will open the arrival dialog and ask the user if they want to go to the next destination
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

        arrivalDialog.setCancelable(false);
        arrivalDialog.show();
    }

    //this method will open the last dialog
    //this will show the message that the ride has come to an end
    private void ShowLastDialog() {
        AlertDialog finalDialog = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.NavAlerts)).create();
        finalDialog.setMessage("The ride has come to an end");
        finalDialog.setButton(AlertDialog.BUTTON_NEGATIVE,"FINISH RIDE",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dInterface, int num) {
                final Intent mainIntent = new Intent(NavigationActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        });

        finalDialog.setCancelable(false);
        finalDialog.show();
    }

    //this method will create the route between the starting point and the next point from the routePoints list
    private void MakeRoute(Point startPoint, Point destPoint) {
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(startPoint)
                .destination(destPoint)
                .alternatives(true)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    //need to get the route from the response of the navroute builder
                    @Override
                    public void onResponse(@NotNull Call<DirectionsResponse> directionCall, @NotNull Response<DirectionsResponse> directionResponse) {
                        DirectionsResponse dResponse = directionResponse.body();
                        if (dResponse != null && !dResponse.routes().isEmpty()) {
                            StartNavView(dResponse.routes().get(0));
                        }
                    }

                    //need to override the onfailure method as the response can fail
                    @Override
                    public void onFailure(Call<DirectionsResponse> directionCall, Throwable t) {
                        Log.e(TAG, "Thrown: " + t.getMessage());
                    }
                });
    }

    //this method will set the ride to completed in the database
    private void SetRideToCompleted(){
        Map<String, Object> rideMap = new HashMap<>();
        rideMap.put("completed", true);

        //update the ride in the database with the completed flag
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

    //need to set the dialogopen flag to false as it will be closed now
    //need to make the navbuilderoptions
    //attach the listeners to the builder
    //flag -- shouldsimroute will simulate the route for demo purposes
    //switching this to false will use proper nav
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

    //this method will get the latest location of the user using the loclisteningcallback
    //this will make it into a point and return it
    private Point GetLatestLocation() {
        return Point.fromLngLat(locListeningCallback.lastLoc.getLongitude(), locListeningCallback.lastLoc.getLatitude());
    }

    //need to create a locationlisteningcallback to get the current users current location
    private static class locListenCallback implements LocationEngineCallback<LocationEngineResult> {

        //need to make a public member variable needs to be public since need to access it later
        public Location lastLoc;

        //constructor takes the activity , not doing anything in the constrcutor though
        locListenCallback(NavigationActivity activity) {

        }

        //get the last location from the result and put this into the lastLoc location object
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
