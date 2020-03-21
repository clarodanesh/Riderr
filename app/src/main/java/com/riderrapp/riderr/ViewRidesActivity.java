package com.riderrapp.riderr;

import android.content.Intent;
import android.os.Build;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewRidesActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {

    private static final String TAG = "ViewRidesActivity";
    // variables for adding location layer
    private MapView viewRidesMap;
    private MapboxMap readyMap;
    // variables for adding location layer
    private PermissionsManager permMngr;
    private LocationComponent userLocComp;
    // variables for calculating and drawing a route
    private DirectionsRoute dRoute;

    private NavigationMapRoute navRoute;
    // variables needed to initialize navigation
    private Button startNavBtn, cancelRideBtn;

    private double lat = 0, lng = 0;
    private String pride = "", dride = "";

    private List<Point> wayPoints = new ArrayList<>();
    private Map<String, Object> offeredRideMapSO = new HashMap<>();
    private Map<String, String> rideDataMapSS = new HashMap<>();
    private Map<String, Object> cancelRideDataMapSO = new HashMap<>();

    private Map<String, Object> cancelRideMapSO = new HashMap<>();
    private Map<String, String> cancelRideInnerMapSS = new HashMap<>();

    final FirebaseFirestore dataStore = FirebaseFirestore.getInstance();
    final FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle instanceState) {
        super.onCreate(instanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_view_rides);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }
        viewRidesMap = findViewById(R.id.viewRidesMap);
        viewRidesMap.onCreate(instanceState);
        viewRidesMap.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap readyMap) {
        this.readyMap = readyMap;
        readyMap.setStyle(new Style.Builder().fromUri("mapbox://styles/diqbal/ck743r90t2pt01io833c6fdpg"), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull final Style loadedStyle) {
                String uid = currUser.getUid();

                AccessLocComp(loadedStyle);
                GetUserRide(loadedStyle, uid);

                startNavBtn = findViewById(R.id.startNavButton);
                cancelRideBtn = findViewById(R.id.cancelRideButton);

                startNavBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent navIntent = new Intent(ViewRidesActivity.this, NavigationActivity.class);

                        navIntent.putExtra(NavigationActivity.RIDE_ID, dride);

                        startActivity(navIntent);
                    }
                });

                cancelRideBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CancelRide();
                    }
                });
            }
        });
    }

    private void SetTextViewText(String d, String dt, String t, String dest){
        final TextView driverLabel = (TextView)findViewById(R.id.driverText);
        TextView dateLabel = (TextView)findViewById(R.id.dateText);
        TextView timeLabel = (TextView)findViewById(R.id.timeText);
        TextView destinationLabel = (TextView)findViewById(R.id.destText);

        DocumentReference userReference = dataStore.collection("users").document(d);
        userReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> returnedTask) {
                if (returnedTask.isSuccessful()) {
                    DocumentSnapshot userDoc = returnedTask.getResult();
                    if (userDoc.exists()) {
                        driverLabel.setText(driverLabel.getText() + userDoc.getString("firstname") + " " + userDoc.getString("lastname"));
                    } else {
                        Log.d(TAG, "couldnt find user doc");
                    }
                } else {
                    Log.d(TAG, "Exception ", returnedTask.getException());
                }
            }
        });

        dateLabel.setText(dateLabel.getText() + dt);
        timeLabel.setText(timeLabel.getText() + t);
        destinationLabel.setText(destinationLabel.getText() + dest);
    }

    private void GetUserRide(final Style loadedStyle, String uid){
        DocumentReference userReference = dataStore.collection("users").document(uid);
        userReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> returnedTask) {
                if (returnedTask.isSuccessful()) {
                    DocumentSnapshot userDoc = returnedTask.getResult();
                    if (userDoc.exists()) {
                        pride = userDoc.getString("p-ride");
                        dride = userDoc.getString("d-ride");

                        if(dride != null) {
                            startNavBtn.setVisibility(View.VISIBLE);
                            cancelRideBtn.setVisibility(View.VISIBLE);
                            SetRide(loadedStyle, dride);
                        }else if(pride != null){
                            startNavBtn.setVisibility(View.GONE);
                            cancelRideBtn.setVisibility(View.VISIBLE);
                            SetRide(loadedStyle, pride);
                        }else{
                            startNavBtn.setVisibility(View.GONE);
                            cancelRideBtn.setVisibility(View.GONE);
                        }
                    } else {
                        Log.d(TAG, "couldnt find user doc");
                    }
                } else {
                    Log.d(TAG, "Exception: ", returnedTask.getException());
                }
            }
        });
    }

    private void CancelRide(){
        DocumentReference userReference = dataStore.collection("users").document(currUser.getUid());
        userReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> returnedTask) {
                if (returnedTask.isSuccessful()) {
                    DocumentSnapshot userDoc = returnedTask.getResult();
                    if (userDoc.exists()) {
                        if(dride != null){
                            dataStore.collection("OfferedRides").document(dride)
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void v) {
                                            Map<String, Object> userMap = new HashMap<>();
                                            userMap.put("d-ride", null);
                                            dataStore.collection("users").document(currUser.getUid())
                                                    .update(userMap)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void v) {
                                                            finish();
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
                        if(pride != null){
                            DocumentReference rideRef = dataStore.collection("OfferedRides").document(pride);
                            rideRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> returnedTask) {
                                    if (returnedTask.isSuccessful()) {
                                        DocumentSnapshot rideDoc = returnedTask.getResult();
                                        if (rideDoc.exists()) {
                                            cancelRideMapSO = rideDoc.getData();
                                            List<Map> d = (List<Map>) cancelRideMapSO.get("passengers");
                                            for(int i = 0; i < d.size(); i++){
                                                cancelRideInnerMapSS = d.get(i);
                                                if(cancelRideInnerMapSS.get("passenger").equals(currUser.getUid())) {
                                                    String s = cancelRideInnerMapSS.get("longitude");
                                                    String st = cancelRideInnerMapSS.get("latitude");

                                                    cancelRideDataMapSO.put("passenger", currUser.getUid());
                                                    cancelRideDataMapSO.put("longitude", s);
                                                    cancelRideDataMapSO.put("latitude", st);
                                                    cancelRideDataMapSO.put("rating", cancelRideInnerMapSS.get("rating"));
                                                    cancelRideDataMapSO.put("amountOfRatings", cancelRideInnerMapSS.get("amountOfRatings"));

                                                    DocumentReference selectedRideRef = dataStore.collection("OfferedRides").document(pride);
                                                    selectedRideRef.update("passengers", FieldValue.arrayRemove(cancelRideDataMapSO));
                                                    selectedRideRef.update("vehicleCapacity", FieldValue.increment(1));
                                                    Map<String, Object> userMap = new HashMap<>();
                                                    userMap.put("p-ride", null);
                                                    dataStore.collection("users").document(currUser.getUid())
                                                            .update(userMap)
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void v) {
                                                                    finish();
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.w(TAG, "cant update details", e);
                                                                }
                                                            });
                                                }
                                            }
                                        } else {
                                            Log.d(TAG, "ride doc cant be found");
                                        }
                                    } else {
                                        Log.d(TAG, "Exception: ", returnedTask.getException());
                                    }
                                }
                            });
                        }
                    } else {
                        Log.d(TAG, "cant get the ride");
                    }
                } else {
                    Log.d(TAG, "Exception: ", returnedTask.getException());
                }
            }
        });
    }

    private void SetRide(final Style loadedStyle, String rideid){
        DocumentReference rideReference = dataStore.collection("OfferedRides").document(rideid);
        rideReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> returnedTask) {
                if (returnedTask.isSuccessful()) {
                    DocumentSnapshot rideDoc = returnedTask.getResult();
                    if (rideDoc.exists()) {
                        SetTextViewText(rideDoc.getString("offeredBy"), rideDoc.getString("date"), rideDoc.getString("time"), rideDoc.getString("destination"));
                        offeredRideMapSO = rideDoc.getData();
                        List<Map> d = (List<Map>) offeredRideMapSO.get("passengers");
                        for(int i = 0; i < d.size(); i++){
                            rideDataMapSS = d.get(i);

                            String s = rideDataMapSS.get("longitude");
                            String st = rideDataMapSS.get("latitude");
                            Double l = Double.parseDouble(s);
                            Double lt = Double.parseDouble(st);
                            Point p = Point.fromLngLat(l, lt);

                            wayPoints.add(Point.fromLngLat(l, lt));
                            System.out.println("tester" + p);

                        }

                        lat = rideDoc.getDouble("latitude");
                        lng = rideDoc.getDouble("longitude");

                        Point destPoint = Point.fromLngLat(lng, lat);

                        Point startPoint = null;
                        if(userLocComp != null) {
                            startPoint = Point.fromLngLat(userLocComp.getLastKnownLocation().getLongitude(), userLocComp.getLastKnownLocation().getLatitude());

                            MakeRoute(startPoint, destPoint);
                        }else{
                            startPoint = Point.fromLngLat(0.1278, 51.5074);
                        }
                    } else {
                        Log.d(TAG, "cant get ride");
                    }
                } else {
                    Log.d(TAG, "Excception: ", returnedTask.getException());
                }
            }
        });
    }

    private Long ConvertSecondsToMinutes(Double secs){
        Double mins = secs / 60;
        return Math.round(mins);
    }

    private Long ConvertDistanceToMiles(Double dist){
        Double miles = dist * 0.000621;
        return Math.round(miles);
    }

    private void MakeRoute(Point start, Point end) {
        NavigationRoute.Builder navRouteBld = NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(start)
                .profile("driving")
                .destination(end);

                if(wayPoints.size() > 0) {
                    for (Point waypoint : wayPoints) {
                        navRouteBld.addWaypoint(waypoint);
                    }
                }

            navRouteBld.build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> dCall, Response<DirectionsResponse> dRes) {
                        if (dRes.body() == null) {
                            Log.e(TAG, "routes body null maybe access token not set");
                            return;
                        } else if (dRes.body().routes().size() < 1) {
                            Log.e(TAG, "routes size is less than 1");
                            return;
                        }

                        dRoute = dRes.body().routes().get(0);
                        Long minutes = ConvertSecondsToMinutes(dRoute.duration());
                        Long miles = ConvertDistanceToMiles(dRoute.distance());

                        TextView etaLabel = (TextView)findViewById(R.id.etaText);
                        TextView ejdLabel = (TextView)findViewById(R.id.ejdText);

                        etaLabel.setText(etaLabel.getText() + minutes.toString() + " minutes");
                        ejdLabel.setText(ejdLabel.getText() + miles.toString() + " miles");

                        // Draw the route on the map
                        if (navRoute != null) {
                            navRoute.updateRouteArrowVisibilityTo(false);
                        } else {
                            navRoute = new NavigationMapRoute(null, viewRidesMap, readyMap, R.style.NavigationMapRoute);
                        }
                        navRoute.addRoute(dRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> dCall, Throwable t) {
                        Log.e(TAG, "Thrown: " + t.getMessage());
                    }
                });
    }

    private void AccessLocComp(@NonNull Style loadedStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            userLocComp = readyMap.getLocationComponent();
            LocationComponentOptions styleLocComp = LocationComponentOptions.builder(this)
                    .elevation(5)
                    .accuracyAlpha(.6f)
                    .accuracyColor(ContextCompat.getColor(this, R.color.colorAccent))
                    .foregroundTintColor(ContextCompat.getColor(this, R.color.colorAccent))
                    .backgroundTintColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    .bearingTintColor(ContextCompat.getColor(this, R.color.colorAccent))
                    .build();

            LocationComponentActivationOptions locCompActivation =
                    LocationComponentActivationOptions.builder(this, loadedStyle)
                            .locationComponentOptions(styleLocComp)
                            .build();

            userLocComp.activateLocationComponent(locCompActivation);
            userLocComp.setLocationComponentEnabled(true);

            // Set the component's camera mode
            userLocComp.setRenderMode(RenderMode.COMPASS);
            userLocComp.setCameraMode(CameraMode.TRACKING);
        } else {
            permMngr = new PermissionsManager(this);
            permMngr.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int reqCode, @NonNull String[] perms, @NonNull int[] grants) {
        permMngr.onRequestPermissionsResult(reqCode, perms, grants);
    }

    @Override
    public void onExplanationNeeded(List<String> permsRequested) {
        if(permsRequested.get(0).equals("android.permission.ACCESS_FINE_LOCATION")) {
            Toast.makeText(this, "Riderr needs to access your location.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPermissionResult(boolean permsGranted) {
        if (permsGranted) {
            AccessLocComp(readyMap.getStyle());
            String uid = currUser.getUid();
            GetUserRide(readyMap.getStyle(), uid);
        } else {
            Toast.makeText(this, "Please allow Riderr to see your location.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        viewRidesMap.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewRidesMap.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewRidesMap.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewRidesMap.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        viewRidesMap.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewRidesMap.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        viewRidesMap.onLowMemory();
    }
}
