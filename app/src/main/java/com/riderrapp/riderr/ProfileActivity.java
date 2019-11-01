package com.riderrapp.riderr;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBar;

import android.view.Menu;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;


public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final String TAG = "ProfileActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        final Button editProfileBtn = (Button) findViewById(R.id.EditProfileBtn);
        final Button saveProfileChangesBtn = (Button) findViewById(R.id.SaveProfileChangesBtn);

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                editProfileBtn.setVisibility(View.GONE);
                saveProfileChangesBtn.setVisibility(View.VISIBLE);
            }
        });

        saveProfileChangesBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveProfileChangesBtn.setVisibility(View.GONE);
                editProfileBtn.setVisibility(View.VISIBLE);
                SaveProfileChanges();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        GetUser();
    }

    private void GetUser(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            Toast.makeText(ProfileActivity.this, "User is signed in.",
                    Toast.LENGTH_SHORT).show();
            updateUI(user);
        } else {
            // No user is signed in
            Toast.makeText(ProfileActivity.this, "NO user is signed in.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void SaveProfileChanges(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = fbuser.getUid();
        Map<String, Object> user = new HashMap<>();
        user.put("user-id", uid);
        user.put("name", "NRG Formal");
        user.put("car-make", "Toyota");
        user.put("registration-no", "FF55 LMN");
        user.put("seats-no", 4);

        // Add a new document with a generated ID
        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        final Button editProfileBtn = (Button) findViewById(R.id.EditProfileBtn);

        if (user != null) {
            System.out.println("user NOT null");
            // Name, email address, and profile photo Url
            /*String name = user.getDisplayName();
            String email = user.getEmail();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            System.out.println(name);
            System.out.println(email);
            System.out.println(emailVerified);

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName("Riderr name test update")
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                System.out.println("profile updated");
                            }
                        }
                    });*/
            //SaveProfileChanges();
            //here instead we want to call get user profile, as every time the profile loads then get the profile
            boolean emailVerified = user.isEmailVerified();

            if(emailVerified) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseUser fbuser = FirebaseAuth.getInstance().getCurrentUser();
                String uid = fbuser.getUid();
                editProfileBtn.setEnabled(true);


                DocumentReference docRef = db.collection("users").document(uid);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getString("car-make"));

                                TextView carMakeLabel = (TextView)findViewById(R.id.CarMakeLabel);
                                carMakeLabel.setText(document.getString("car-make"));

                                TextView carRegLabel = (TextView)findViewById(R.id.CarRegLabel);
                                carRegLabel.setText(document.getString("registration-no"));

                                TextView carSeatsLabel = (TextView)findViewById(R.id.CarSeatsLabel);
                                int seatsNo = document.getLong("seats-no").intValue();
                                String seatsNoString = String.valueOf(seatsNo);
                                carSeatsLabel.setText(seatsNoString);

                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
            }
            else{
                editProfileBtn.setEnabled(false);
                Toast.makeText(ProfileActivity.this, "You need to VERIFY your email before you can make changes to your profile.",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            System.out.println("user IS null");
            editProfileBtn.setEnabled(false);
        }
    }



}
