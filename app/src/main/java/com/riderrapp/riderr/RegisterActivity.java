package com.riderrapp.riderr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    //member variables for the registeractivity class
    private static final String TAG = "RegisterActivity";
    private FirebaseAuth authInstance;
    final FirebaseFirestore dataStore = FirebaseFirestore.getInstance();
    final FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle instanceState) {
        super.onCreate(instanceState);
        setContentView(R.layout.activity_register);

        //set the background color for the register activity
        getWindow().getDecorView().setBackgroundColor(getColor(R.color.colorPrimaryDark));
        //if the device sdk is >= 21 then the nav bar color can be changed
        if(Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(getColor(R.color.colorPrimaryDark));
        }

        authInstance = FirebaseAuth.getInstance();
        final EditText emailEdit =  (EditText) findViewById(R.id.emailTxtBoxRegister);
        final EditText passwordEdit =  (EditText) findViewById(R.id.passwordTextBoxRegister);
        final Button registerBtn = (Button) findViewById(R.id.RegisterBtnRegister);

        //set listener for the register button
        registerBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String email = (String) emailEdit.getText().toString();
                String password = (String) passwordEdit.getText().toString();

                email = email.replace(" ", "");
                password = password.replace(" ", "");

                //after filtering what the user typed then send for register
                register(email, password);
            }
        });

        //create a login intent for people who already have an account and open that intent and close this one
        final Intent loginIntent = new Intent(this, LoginActivity.class);
        final Button loginRegisterBtn = (Button) findViewById(R.id.loginBtnRegister);
        loginRegisterBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(loginIntent);
                finish();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    //Email validation regex function made with help from
    //https://www.tutorialspoint.com/validate-email-address-in-java
    static boolean IsEmailValid(String emailToCheck) {
        String regularExpression = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return emailToCheck.matches(regularExpression);
    }

    //this method will register the user using a firebase method
    public void register(final String e, final String p){
        //check if the email contains .ac.uk and if the email is a valid email
        if(e.contains(".ac.uk") && IsEmailValid(e)) {
            //if the password length is greater than 7 chars then allow register
            if(p.length() > 7) {
                //register the user
                authInstance.createUserWithEmailAndPassword(e, p)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> returnedTask) {
                                if (returnedTask.isSuccessful()) {
                                    FirebaseUser user = authInstance.getCurrentUser();
                                    TryLogin(e, p);
                                    Toast.makeText(RegisterActivity.this, "Registration successful.", Toast.LENGTH_SHORT).show();
                                    CheckIfUserNull(user);
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Authentication failed. Try again.", Toast.LENGTH_SHORT).show();
                                    CheckIfUserNull(null);
                                }
                            }
                        });
            }else{
                Toast.makeText(RegisterActivity.this, "Your password must be longer than 7 characters.", Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(RegisterActivity.this, "Your email needs to be a valid .ac.uk email.", Toast.LENGTH_LONG).show();
        }
    }

    //if the user is null then dont send verification to the users email
    private void CheckIfUserNull(FirebaseUser u) {
        if (u != null) {
            u.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> returnedTask) {
                            if (returnedTask.isSuccessful()) {
                                Toast.makeText(RegisterActivity.this, "Check email for verification", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            System.out.println("The user returned null");
        }
    }

    //try loggin the user in, need to do this so that the login activity can be opened
    private void TryLogin(String email, String password){
        authInstance.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> returnedTask) {
                        if (returnedTask.isSuccessful()) {
                            FirebaseUser user = authInstance.getCurrentUser();
                            CreateUser(user);
                            AttemptLoginOpen(user);
                        } else {
                            Toast.makeText(RegisterActivity.this, "Registration Failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //when registering create a map that will add the user details to the db
    private void CreateUser(FirebaseUser u){
        String uid = u.getUid();
        Map<String, Object> userMap = new HashMap<>();

        //add the user details to the userMap
        userMap.put("car-make", "");
        userMap.put("firstname", "firstname");
        userMap.put("lastname", "lastname");
        userMap.put("registration-no", "");
        userMap.put("seats-no", 0);
        userMap.put("user-email", u.getEmail());
        userMap.put("user-id", u.getUid());
        userMap.put("p-ride", null);
        userMap.put("d-ride", null);
        userMap.put("latitude", null);
        userMap.put("longitude", null);
        userMap.put("rating", -1);
        userMap.put("amountOfRatings", 0);
        userMap.put("ride-price", null);

        //set the details into the database
        dataStore.collection("users").document(uid)
                .set(userMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        Log.d(TAG, "User was added to the db");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Exception: ", e);
                    }
                });
    }

    //Open the login activity  by signing out after the suer had been signed in, as they need to verify
    private void AttemptLoginOpen(FirebaseUser u){
        if (u != null) {
            FirebaseAuth.getInstance().signOut();
            final Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        } else {
            System.out.println("returned null user");
        }
    }
}
