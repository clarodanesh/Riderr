package com.riderrapp.riderr;

import android.content.Intent;
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

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBar;

import android.view.Menu;
import android.widget.Button;

import timber.log.Timber;


public class ViewRidesActivity extends AppCompatActivity {

    private static final String TAG = "ViewRidesActivity";

    private MapView mainMapView;
    private SymbolManager symbolManager;
    private Symbol symbol;
    private static final String ID_ICON_MARKER = "marker";
    private double lat = 0, lng = 0;
    private String pride = "", dride = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1IjoiZGlxYmFsIiwiYSI6ImNqdzZtMzQ3czAxZXYzem83eTY4NWpua2kifQ.clrFPQXs0E70rz9R4H292w");
        setContentView(R.layout.activity_view_rides);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mainMapView = findViewById(R.id.mapView);
        mainMapView.onCreate(savedInstanceState);
        mainMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                mapboxMap.getUiSettings().setAllGesturesEnabled(false);
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull final Style style) {
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
                                        pride = document.getString("p-ride");
                                        //dride = document.getLong("longitude");
                                        dride = document.getString("d-ride");

                                        if(dride != null) {
                                            SetRide(style, mapboxMap, dride);
                                        }else if(pride != null){
                                            SetRide(style, mapboxMap, pride);
                                        }else{

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
                });
            }
        });
    }

    private void SetRide(final Style style, final MapboxMap mapboxMap, String rideid){
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("OfferedRides").document(rideid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        lat = document.getDouble("latitude");
                        //dride = document.getLong("longitude");
                        lng = document.getDouble("longitude");


                        AddAnnotationMarkerToStyle(style);
                        // create symbol manager
                        GeoJsonOptions geoJsonOptions = new GeoJsonOptions().withTolerance(0.4f);
                        symbolManager = new SymbolManager(mainMapView, mapboxMap, style, null, geoJsonOptions);
                        symbolManager.deleteAll();

                        symbolManager = new SymbolManager(mainMapView, mapboxMap, style, null, geoJsonOptions);
                        // set non data driven properties
                        symbolManager.setIconAllowOverlap(true);
                        symbolManager.setTextAllowOverlap(true);

                        // create a symbol
                        SymbolOptions symbolOptions = new SymbolOptions()
                                .withLatLng(new LatLng(lat, lng))
                                .withIconImage(ID_ICON_MARKER)
                                .withIconSize(1f)
                                .withSymbolSortKey(10.0f);
                        //.withDraggable(true);
                        symbol = symbolManager.create(symbolOptions);
                        Timber.e(symbol.toString());

                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(new LatLng(lat, lng))
                                        .zoom(8)
                                        .build()), 4000);
                    } else {
                        Log.d(TAG, "No such document set ride");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void AddAnnotationMarkerToStyle(Style style) {
        style.addImage(ID_ICON_MARKER,
                BitmapUtils.getBitmapFromDrawable(ContextCompat.getDrawable(this, R.drawable.ic_riderr_launcher_foreground)));
    }

    @Override
    public void onStart() {
        super.onStart();
        mainMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mainMapView.onResume();

        mainMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                mapboxMap.getUiSettings().setAllGesturesEnabled(false);
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull final Style style) {
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
                                        pride = document.getString("p-ride");
                                        //dride = document.getLong("longitude");
                                        dride = document.getString("d-ride");

                                        if(dride != null) {
                                            SetRide(style, mapboxMap, dride);
                                        }else if(pride != null){
                                            SetRide(style, mapboxMap, pride);
                                        }else{

                                        }


                                    } else {
                                        Log.d(TAG, "No such document on resume");
                                    }
                                } else {
                                    Log.d(TAG, "get failed with ", task.getException());
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
        mainMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mainMapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mainMapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainMapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mainMapView.onSaveInstanceState(outState);
    }
}
