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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
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

import android.view.Menu;
import android.widget.Button;
import androidx.cardview.widget.CardView;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

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
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        final Intent searchIntent = new Intent(this, SearchActivity.class);
        Button searchButton = (Button) findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                startActivity(searchIntent);
            }
        });

        final Intent offerRideIntent = new Intent(this, OfferRideActivity.class);
        Button offerRideButton = (Button) findViewById(R.id.offerRideButton);
        offerRideButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                startActivity(offerRideIntent);
            }
        });

        /*Button viewRideButton = (Button) findViewById(R.id.viewRideButton);
        viewRideButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                new AlertDialog.Builder(v.getContext()).setMessage("You have no Rides").show();
            }
        });*/

        //TAKING THE CARD VIEW ON CLICK LISTENER OUT
        /*CardView card_view = (CardView) findViewById(R.id.card_view); // creating a CardView and assigning a value.
        card_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do whatever you want to do on click (to launch any fragment or activity you need to put intent here.)
                //if(journey is not set then dont navigate anywhere)
                //else if(journey is driver journey, navigate to driver journey activity with start navigation button)
                //else if(journey is riderr journey, navigate to riderr journey activity  with showing route on map and times)
                new AlertDialog.Builder(v.getContext()).setMessage("You have no Rides").show();

                //taken out here
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setCancelable(false);
                dialog.setTitle("Dialog on Android");
                dialog.setMessage("Are you sure you want to delete this entry?" );
                dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Action for "Delete".
                    }
                })
                        .setNegativeButton("Cancel ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Action for "Cancel".
                            }
                        });

                final AlertDialog alert = dialog.create();
                alert.show();
                //taken out here
            }
        });*/

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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_far) {
            final Intent searchIntent = new Intent(this, SearchActivity.class);
            startActivity(searchIntent);
            return true;
        } else if (id == R.id.nav_oar) {
            final Intent offerRideIntent = new Intent(this, OfferRideActivity.class);
            startActivity(offerRideIntent);
            return true;
        } else if (id == R.id.nav_vr) {
            final Intent viewRidesIntent = new Intent(this, ViewRidesActivity.class);
            startActivity(viewRidesIntent);
            return true;
        } else if (id == R.id.nav_profile) {
            final Intent profileIntent = new Intent(this, ProfileActivity.class);
            startActivity(profileIntent);
            return true;
        } else if (id == R.id.nav_logout) {
            //final Intent splashIntent = new Intent(this, SplashActivity.class);
            //startActivity(splashIntent);
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;
        } else if (id == R.id.nav_send) {
            final Intent Intent2 = new Intent(this, FoundRidesActivity.class);
            startActivity(Intent2);
            return true;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
                mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/diqbal/ck743r90t2pt01io833c6fdpg"), new Style.OnStyleLoaded() {
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
