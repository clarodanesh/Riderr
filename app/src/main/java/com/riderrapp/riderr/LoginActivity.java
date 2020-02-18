package com.riderrapp.riderr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        /*if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);*/
        getWindow().getDecorView().setBackgroundColor(getColor(R.color.colorPrimaryDark));
        if(Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(getColor(R.color.colorPrimaryDark));
        }
        mAuth = FirebaseAuth.getInstance();
        final EditText emailEdit =  (EditText) findViewById(R.id.emailTxtBoxLogin);
        final EditText passwordEdit =  (EditText) findViewById(R.id.passwordTextBoxLogin);
        final Button loginBtn = (Button) findViewById(R.id.loginBtn);


        loginBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String email = (String) emailEdit.getText().toString();
                String password = (String) passwordEdit.getText().toString();

                email = email.replace(" ", "");
                password = password.replace(" ", "");

                DoFirebaseLogin(email, password);
            }
        });

        final Intent RegisterIntent = new Intent(this, RegisterActivity.class);
        //handle search ride button
        final Button registerLoginBtn = (Button) findViewById(R.id.registerBtnLogin);
        registerLoginBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                //startActivity(intent);

                //here can onclick get the fulldate and time and onlclick send to server
                //dateText.setText(fullDate);
                startActivity(RegisterIntent);
                finish();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        /*// Check if user is signed in (non-null) and update UI accordingly.
        String email = "riderrmail@gmail.com";
        String password = "test@riderrappcom";

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            Toast.makeText(RegisterActivity.this, "Authentication SUCCESS.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });

        //FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);*/
    }

    public void DoFirebaseLogin(String e, String p){
        mAuth.signInWithEmailAndPassword(e, p)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            boolean ver = user.isEmailVerified();
                            String checker = String.valueOf(ver);
                            Toast.makeText(LoginActivity.this, checker,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            System.out.println("user NOT null");
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
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
                    });

            if(emailVerified) {
                final Intent mainIntent = new Intent(this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }else {
                Toast.makeText(LoginActivity.this, "Check your email for a verification",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            System.out.println("user IS null");
        }
    }
}
