package com.riderrapp.riderr;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

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
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //Member variables for the mainactivity class
    private static final String TAG = "MainActivity";
    private MapView mainActivityMap;
    private SymbolManager mapMarkerManager;
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
        //using the mapbox api in this activity so need to get and instance using my access token
        Mapbox.getInstance(this, "pk.eyJ1IjoiZGlxYmFsIiwiYSI6ImNqdzZtMzQ3czAxZXYzem83eTY4NWpua2kifQ.clrFPQXs0E70rz9R4H292w");
        setContentView(R.layout.activity_main);

        //mainactivity toolbar which holds the nav drawer opener
        Toolbar mainActivityToolbar = findViewById(R.id.mainActivityToolbar);
        setSupportActionBar(mainActivityToolbar);

        //need to create a drawer layout for the nav drawer
        DrawerLayout navDrawer = findViewById(R.id.mainActivityNavDrawerLayout);
        NavigationView navDrawerView = findViewById(R.id.navDrawerView);
        ActionBarDrawerToggle navDrawerToggler = new ActionBarDrawerToggle(this, navDrawer, mainActivityToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        navDrawer.addDrawerListener(navDrawerToggler);
        navDrawerToggler.syncState();
        navDrawerView.setNavigationItemSelectedListener(this);

        //two intents as there are two buttons that nav to offer rides and search rides activities
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

        //need to set the map view to the main acitvity map object so that I can use the methods
        mainActivityMap = findViewById(R.id.activeRideMapView);
        mainActivityMap.onCreate(instanceState);
        //get the map and open it into the map view
        mainActivityMap.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap readyMap) {
                //main map view doesnt need to be interacted with as it is a view only map
                //so Ive set the gestures to disabled
                readyMap.getUiSettings().setAllGesturesEnabled(false);
                //I have set the style of the map using a custom one created using mapbox studio
                readyMap.setStyle(new Style.Builder().fromUri("mapbox://styles/diqbal/ck743r90t2pt01io833c6fdpg"), new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull final Style loadedStyle) {
                        //once everything for the map is done and style is set I can now continue with the rest of the app
                        String uid = currUser.getUid();

                        //now I need to get the current users data from the db and check if they have a driver or passenger ride set
                        DocumentReference usersReference = dataStore.collection("users").document(uid);
                        usersReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> returnedTask) {
                                if (returnedTask.isSuccessful()) {
                                    DocumentSnapshot userDocument = returnedTask.getResult();
                                    if (userDocument.exists()) {
                                        pride = userDocument.getString("p-ride");
                                        dride = userDocument.getString("d-ride");

                                        //handling the driver and passenger rides differently so thats why I check for them
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

    //Nav drawer has different options user can click so I need to check which option is selected
    //and handle appropriately
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

    //this method will do one of three things
    //1. check if the user has a driver ride set and it has been completed, if so then it will show a rating dialog
    //2. check if the user has a passenger ride set and has been completed, if so show the rating dialog to rate the driver
    //3. display the destination of the ride into the map view
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
                            //check if the user has a driver ride set and it has been completed, if so then it will show a rating dialog
                            showDriverRatingDialog(rideid, rideType);
                        }else if(rideComplete && rideType.equals("passenger")){
                            //check if the user has a passenger ride set and has been completed, if so show the rating dialog to rate the driver
                            showPassRatingDialog(rideid, rideType);
                        }else {
                            //display the destination of the ride into the map view
                            //anything that I want to add to the map now needs to be inserted into it
                            //here I am going to insert a marker for the users ride destination
                            InsertMarkerToStyle(loadedStyle);

                            //I need to create a symbol manager and delete all symbols incase there are any already on the map
                            GeoJsonOptions gjOptions = new GeoJsonOptions().withTolerance(0.4f);
                            mapMarkerManager = new SymbolManager(mainActivityMap, readyMap, loadedStyle, null, gjOptions);
                            mapMarkerManager.deleteAll();

                            //I need to create a new symbol manager and use this one to show the icon on the map
                            mapMarkerManager = new SymbolManager(mainActivityMap, readyMap, loadedStyle, null, gjOptions);
                            //allow the icon to overlap any textor icons displayed on the map
                            mapMarkerManager.setIconAllowOverlap(true);
                            mapMarkerManager.setTextAllowOverlap(true);

                            //Now need to set the details of the icon I will set the:
                            //lat and long which is lat lng of the destination
                            //set a symbol sort key which determines priority of placement, items with lower sort key will be below this
                            //icon image using the drawable for the marker
                            //and finally the size
                            SymbolOptions mapMarkerSymbolOptions = new SymbolOptions()
                                    .withLatLng(new LatLng(lat, lng))
                                    .withSymbolSortKey(10.0f)
                                    .withIconImage(MARKER_ID)
                                    .withIconSize(1f);

                            //all that is left to do now is to use the managers create method to create this icon
                            mapMarkerManager.create(mapMarkerSymbolOptions);

                            //in the xml i have to set a default lat lng so whenever this map loads it will go from there
                            //i want the camera to animate over to the icon rather than just teleport
                            readyMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                    new CameraPosition.Builder()
                                            .target(new LatLng(lat, lng))
                                            .zoom(8)
                                            .build()), 500);
                        }
                    } else {
                        //if a ride cant be found this means the driver cancelled it, let the user know
                        ShowRideCancelledDialog();
                    }
                } else {
                    Log.d(TAG, "Exception: ", returnedTask.getException());
                }
            }
        });
    }

    //method to insert the market onto the mapbox map style
    private void InsertMarkerToStyle(Style loadedStyle) {
        loadedStyle.addImage(MARKER_ID, BitmapUtils.getBitmapFromDrawable(ContextCompat.getDrawable(this, R.drawable.map_marker_dark)));
    }

    @Override
    public void onStart() {
        super.onStart();
        mainActivityMap.onStart();
    }

    //overriding the on resume method since the map needs to be update oncreate and onresume
    @Override
    public void onResume() {
        super.onResume();
        mainActivityMap.onResume();

        //just doing exactly the same as what I did in oncreate in onresume
        //reason being that if the user changes their ride option and comes back here it needs to be updated
        //as resume is fired not create
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

    //need to override:
    //onpause
    //onstop
    //onlowmemory
    //ondestroy
    //onsaveinstancestate
    //these need to be overriden according to the mapbox docs for the mapview

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

    //Once the driver or passenger have rated, need to delete the ride from the db
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

    //this method will show the user that the ride has been cancelled
    //uses android AlertDialog object to show an alertdialog that the user can interact with
    private void ShowRideCancelledDialog(){
        //need to use the builder since I want my own custom style used for the alertdialog
        AlertDialog rideCancelledDialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.NavAlerts)).create();
        //setting the message
        rideCancelledDialog.setMessage("Your previous ride was cancelled");
        //setting the positive button which is on the right of the negative button always
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

        //i have set the dialog so that the user can not cancel it and must select an option
        rideCancelledDialog.setCancelable(false);
        rideCancelledDialog.show();
    }

    //this method will show the rating dialog that drivers fill
    //uses android AlertDialog object to show an alertdialog that the user can interact with
    private void showDriverRatingDialog(final String rid, final String rideType) {
        //this dialog will contain an input field so need to declare it
        final EditText driverRatingInput = new EditText(this);

        //need to set the custom dialog for the user using my own style not androids default one
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
                                    //When the driver submits a rating, I need to iterate over each passenger and each passengers details
                                    //then set the ratings using this
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

                                        //update the users ratings in the database so need to use firebase firestore methods
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
                                    //since the driver has submitted a rating the ride can be removed from their profile now
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
                    //if there was an error dismiss the rating dialog and show an error dialog
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

        //need to create linear layout which will contain the text input
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        //the input should fill the alert dialog box
        driverRatingInput.setLayoutParams(lp);
        driverRatingDialog.setView(driverRatingInput);

        //I need to set the text inputs theme programmatically since there is no view in xml created for it
        ColorStateList csList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent));
        ViewCompat.setBackgroundTintList(driverRatingInput, csList);
        driverRatingInput.setTextColor(csList);

        driverRatingDialog.setCancelable(false);
        driverRatingDialog.show();
    }

    //this method will show the rating dialog that passengers fill
    //uses android AlertDialog object to show an alertdialog that the user can interact with
    private void showPassRatingDialog(final String rid, final String rideType) {
        //this dialog will contain an input field so need to declare it
        final EditText passRatingInput = new EditText(this);

        //need to set the custom dialog for the user using my own style not androids default one
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
                                                    //need to update the rating once it has been submitted
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
                    //if there was an error dismiss the rating dialog and show an error dialog
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

        //need to create linear layout which will contain the text input
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        //the input should fill the alert dialog box
        passRatingInput.setLayoutParams(lp);
        passRatingDialog.setView(passRatingInput);

        //I need to set the text inputs theme programmatically since there is no view in xml created for it
        ColorStateList csList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorAccent));
        ViewCompat.setBackgroundTintList(passRatingInput, csList);
        passRatingInput.setTextColor(csList);

        passRatingDialog.setCancelable(false);
        passRatingDialog.show();
    }

    //this method will update the rating of the driver once the passenger submits it
    private void UpdateRating(final String userId, long rating, long amtOfRatings, int ratingAsInt, final String rideType){
        final long amtOfRatingsIncludingThis = amtOfRatings + 1;
        final long ratingToAddToDB;
        final float ratingBeforeRounding;
        final long accumulatedRating;

        //if the rating is -1 then the driver has never been rated
        //need to check for this as calc is different
        if(rating == -1){
            ratingToAddToDB = ratingAsInt / 1;
        }else{
            accumulatedRating = amtOfRatings * rating;
            ratingBeforeRounding = ((float)ratingAsInt + (float)accumulatedRating) / (float)amtOfRatingsIncludingThis;
            ratingToAddToDB = Math.round(ratingBeforeRounding);
        }

        //submit the rating to the db and remove it from the passenger using firebase db methods
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
