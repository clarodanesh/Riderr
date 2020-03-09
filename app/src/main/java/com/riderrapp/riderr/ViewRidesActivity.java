package com.riderrapp.riderr;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
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
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.matrix.v1.MapboxMatrix;
import com.mapbox.api.matrix.v1.models.MatrixResponse;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBar;

import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.graphics.BitmapFactory.decodeResource;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;


public class ViewRidesActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener, PermissionsListener {
    // variables for adding location layer
    private MapView mapView;
    private MapboxMap mapboxMap;
    // variables for adding location layer
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    // variables for calculating and drawing a route
    private DirectionsRoute currentRoute;
    private static final String TAG = "ViewRidesActivity";
    private NavigationMapRoute navigationMapRoute;
    // variables needed to initialize navigation
    private Button button;

    private double lat = 0, lng = 0;
    private String pride = "", dride = "";

    private List<Point> wayPoints = new ArrayList<>();
    private Map<String, Object> cData = new HashMap<>();
    private Map<String, String> uData = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_view_rides);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/diqbal/ck743r90t2pt01io833c6fdpg"), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull final Style style) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
                String uid = fbuser.getUid();

                enableLocationComponent(style);

                //addDestinationIconSymbolLayer(style);

                mapboxMap.addOnMapClickListener(ViewRidesActivity.this);

                GetUserRide(style, db, uid);

                button = findViewById(R.id.startNavButton);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*boolean simulateRoute = true;
                        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                                .directionsRoute(currentRoute)
                                .shouldSimulateRoute(simulateRoute)
                                .build();
// Call this method with Context from within an Activity
                        NavigationLauncher.startNavigation(ViewRidesActivity.this, options);*/
                        Intent intent = new Intent(ViewRidesActivity.this, NavigationActivity.class);

                        intent.putExtra(NavigationActivity.RIDE_ID, dride);

                        startActivity(intent);
                    }
                });


                //button.setEnabled(true);
                //button.setVisibility(View.VISIBLE);
                //button.setBackgroundResource(R.color.design_default_color_primary_dark);
            }
        });
    }

    private void SetTextViewText(String d, String dt, String t, String dest){
        TextView driverLabel = (TextView)findViewById(R.id.driverText);
        TextView dateLabel = (TextView)findViewById(R.id.dateText);
        TextView timeLabel = (TextView)findViewById(R.id.timeText);
        TextView destinationLabel = (TextView)findViewById(R.id.destText);

        driverLabel.setText(driverLabel.getText() + d);
        dateLabel.setText(dateLabel.getText() + dt);
        timeLabel.setText(timeLabel.getText() + t);
        destinationLabel.setText(destinationLabel.getText() + dest);
    }

    private void GetUserRide(final Style style, FirebaseFirestore db, String uid){
        DocumentReference docRef = db.collection("users").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        pride = document.getString("p-ride");
                        //dride = document.getLong("longitude");
                        dride = document.getString("d-ride");

                        if(dride != null) {
                            button.setVisibility(View.VISIBLE);
                            SetRide(style, dride);
                        }else if(pride != null){
                            button.setVisibility(View.GONE);
                            SetRide(style, pride);
                        }else{
                            button.setVisibility(View.GONE);
                        }


                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void SetRide(final Style style, String rideid){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("OfferedRides").document(rideid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        SetTextViewText(document.getString("offeredBy"), document.getString("date"), document.getString("time"), document.getString("destination"));
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

                            wayPoints.add(Point.fromLngLat(l, lt));
                            System.out.println("tester" + p);

                        }

                        lat = document.getDouble("latitude");
                        //dride = document.getLong("longitude");
                        lng = document.getDouble("longitude");


                        Point destinationPoint = Point.fromLngLat(lng, lat);

                        Point originPoint = null;
                        if(locationComponent != null) {
                            originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                                    locationComponent.getLastKnownLocation().getLatitude());

                            GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
                            if (source != null) {
                                source.setGeoJson(Feature.fromGeometry(destinationPoint));
                            }

                            getRoute(originPoint, destinationPoint);
                        }else{
                            originPoint = Point.fromLngLat(0.1278, 51.5074);
                        }


                    } else {
                        Log.d(TAG, "No such document set ride");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("destination-icon-id",
                decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        loadedMapStyle.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
        destinationSymbolLayer.withProperties(
                iconImage("destination-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        loadedMapStyle.addLayer(destinationSymbolLayer);
    }

    @SuppressWarnings( {"MissingPermission"})
    @Override
    public boolean onMapClick(@NonNull LatLng point) {

        /*Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                locationComponent.getLastKnownLocation().getLatitude());

        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
        if (source != null) {
            source.setGeoJson(Feature.fromGeometry(destinationPoint));
        }

        getRoute(originPoint, destinationPoint);
        button.setEnabled(true);
        button.setBackgroundResource(R.color.design_default_color_primary_dark);*/
        return true;
    }

    private Long ConvertSecondsToMinutes(Double secs){
        Double mins = secs / 60;
        return Math.round(mins);
    }

    private Long ConvertDistanceToMiles(Double dist){
        Double miles = dist * 0.000621;
        return Math.round(miles);
    }

    private void getRoute(Point origin, Point destination) {
        NavigationRoute.Builder bld = NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .profile("driving")
                .destination(destination);

                if(wayPoints.size() > 0) {
                    for (Point waypoint : wayPoints) {
                        bld.addWaypoint(waypoint);
                    }
                }

                bld.build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
// You can get the generic HTTP info about the response
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null) {
                            Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e(TAG, "No routes found");
                            return;
                        }

                        currentRoute = response.body().routes().get(0);
                        Long minutes = ConvertSecondsToMinutes(currentRoute.duration());
                        Long miles = ConvertDistanceToMiles(currentRoute.distance());

                        TextView etaLabel = (TextView)findViewById(R.id.etaText);
                        TextView ejdLabel = (TextView)findViewById(R.id.ejdText);

                        etaLabel.setText(etaLabel.getText() + minutes.toString() + " minutes");
                        ejdLabel.setText(ejdLabel.getText() + miles.toString() + " miles");

// Draw the route on the map
                        if (navigationMapRoute != null) {
                            //navigationMapRoute.removeRoute();
                            navigationMapRoute.updateRouteArrowVisibilityTo(false);
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                    }
                });
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {

        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
// Activate the MapboxMap LocationComponent to show user location
// Adding in LocationComponentOptions is also an optional parameter
            locationComponent = mapboxMap.getLocationComponent();
            LocationComponentOptions customLocationComponentOptions = LocationComponentOptions.builder(this)
                    .elevation(5)
                    .accuracyAlpha(.6f)
                    .accuracyColor(ContextCompat.getColor(this, R.color.colorAccent))
                    .foregroundTintColor(ContextCompat.getColor(this, R.color.colorAccent))
                    .backgroundTintColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    .bearingTintColor(ContextCompat.getColor(this, R.color.colorAccent))   //DO ALL THIS WITH XML
                    .build();

            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this, loadedMapStyle)
                            .locationComponentOptions(customLocationComponentOptions)
                            .build();
            locationComponent.activateLocationComponent(locationComponentActivationOptions);
            locationComponent.setLocationComponentEnabled(true);
// Set the component's camera mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
            locationComponent.setCameraMode(CameraMode.TRACKING);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsRequested) {
        if(permissionsRequested.get(0).equals("android.permission.ACCESS_FINE_LOCATION")) {
            Toast.makeText(this, "Riderr needs to access your location.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPermissionResult(boolean permissionGranted) {
        if (permissionGranted) {
            enableLocationComponent(mapboxMap.getStyle());
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
            String uid = fbuser.getUid();
            GetUserRide(mapboxMap.getStyle(), db, uid);
        } else {
            Toast.makeText(this, "Please allow Riderr to see your location.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
