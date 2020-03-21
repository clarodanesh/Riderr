package com.riderrapp.riderr;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    final FirebaseFirestore dataStore = FirebaseFirestore.getInstance();
    final FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle instanceState) {
        super.onCreate(instanceState);
        setContentView(R.layout.activity_profile);
        ActionBar topBar = getSupportActionBar();
        topBar.setDisplayHomeAsUpEnabled(true);

        final Button editProfileBtn = (Button) findViewById(R.id.EditProfileBtn);
        final Intent editProfileIntent = new Intent(this, EditProfileActivity.class);

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(editProfileIntent);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        GetUser();
    }

    private void GetUser(){
        if (currUser != null) {
            updateUI(currUser);
        } else {
            updateUI(null);
        }
    }

    private void updateUI(FirebaseUser u) {
        final Button editProfileBtn = (Button) findViewById(R.id.EditProfileBtn);

        if (u != null) {
            System.out.println("user isnt null");
            boolean emailVerified = u.isEmailVerified();

            if(emailVerified) {
                String uid = currUser.getUid();
                editProfileBtn.setEnabled(true);

                DocumentReference userRef = dataStore.collection("users").document(uid);
                userRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> returnedTask) {
                        if (returnedTask.isSuccessful()) {
                            DocumentSnapshot userDoc = returnedTask.getResult();
                            if (userDoc.exists()) {
                                TextView fnameLabel = (TextView)findViewById(R.id.FirstNameLabel);
                                TextView lnameLabel = (TextView)findViewById(R.id.LastNameLabel);
                                TextView emailLabel = (TextView)findViewById(R.id.EmailLabel);
                                TextView carMakeLabel = (TextView)findViewById(R.id.CarMakeLabel);
                                TextView carRegLabel = (TextView)findViewById(R.id.CarRegLabel);
                                TextView carSeatsLabel = (TextView)findViewById(R.id.CarSeatsLabel);
                                TextView ratingCaption = (TextView)findViewById(R.id.ratingCaption);
                                TextView priceLabel = (TextView)findViewById(R.id.PriceLabel);

                                fnameLabel.setText(userDoc.getString("firstname"));
                                lnameLabel.setText(userDoc.getString("lastname"));
                                emailLabel.setText(userDoc.getString("user-email"));
                                carMakeLabel.setText(userDoc.getString("car-make"));
                                carRegLabel.setText(userDoc.getString("registration-no"));
                                String rCapString;

                                if(userDoc.getLong("rating") == -1){
                                    rCapString = "NA";
                                    ratingCaption.setTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.black_overlay));
                                    ratingCaption.setText(rCapString);
                                }else{
                                    rCapString = Long.toString(userDoc.getLong("rating"));
                                    ratingCaption.setText(rCapString + "/5");
                                    long ratingLong = userDoc.getLong("rating");

                                    if(ratingLong > 3){
                                        ratingCaption.setTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.goodGreen));
                                    }else if(ratingLong == 3){
                                        ratingCaption.setTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.okayAmber));
                                    }else if(ratingLong < 3){
                                        ratingCaption.setTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.badRed));
                                    }
                                }

                                if(userDoc.get("ride-price") == null){
                                    priceLabel.setText("");
                                }else{
                                    priceLabel.setText(userDoc.getString("ride-price"));
                                }

                                int seatsNo = userDoc.getLong("seats-no").intValue();
                                String seatsNoString = String.valueOf(seatsNo);
                                carSeatsLabel.setText(seatsNoString);
                            } else {
                                Log.d(TAG, "couldnt find the user document");
                            }
                        } else {
                            Log.d(TAG, "Exception: ", returnedTask.getException());
                        }
                    }
                });
            }
            else{
                editProfileBtn.setEnabled(false);
                Toast.makeText(ProfileActivity.this, "You need to VERIFY your email before you can make changes to your profile.", Toast.LENGTH_SHORT).show();
            }
        } else {
            System.out.println("user returned null");
            editProfileBtn.setEnabled(false);
        }
    }



}
