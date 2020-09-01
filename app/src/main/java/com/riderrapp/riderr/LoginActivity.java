package com.riderrapp.riderr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    //member variables for the loginactivity class, just an authorisation instance from firebase
    private FirebaseAuth authInstance;

    @Override
    protected void onCreate(Bundle instanceState) {
        super.onCreate(instanceState);
        setContentView(R.layout.activity_login);

        //need to set the background color to the blue color to fit my apps color scheme
        getWindow().getDecorView().setBackgroundColor(getColor(R.color.colorPrimaryDark));

        //navigation bar color change can only be done if the device supports android SDK 21 or above
        //so need to check for this first before changing the color
        if(Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(getColor(R.color.colorPrimaryDark));
        }

        //need to actually assign an instance to the authInstance object now
        authInstance = FirebaseAuth.getInstance();

        //will be getting the text from the edittexts so need to assign them to edittext objects here
        final EditText emailEdit =  (EditText) findViewById(R.id.emailTxtBoxLogin);
        final EditText passwordEdit =  (EditText) findViewById(R.id.passwordTextBoxLogin);

        //will be setting an onclicklistener to the login btn so need to do this here
        final Button loginBtn = (Button) findViewById(R.id.loginBtn);

        //the onclicklistener will get the text from the textviews and then try and log the user in
        loginBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String email = (String) emailEdit.getText().toString();
                String password = (String) passwordEdit.getText().toString();

                email = email.replace(" ", "");
                password = password.replace(" ", "");

                DoLogin(email, password);
            }
        });

        //if the user doesnt have an account need to show them the register button to register
        //creates a new intent then opens the register activity and finishes this one
        final Intent registerIntent = new Intent(this, RegisterActivity.class);
        final Button registerLoginBtn = (Button) findViewById(R.id.registerBtnLogin);
        registerLoginBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(registerIntent);
                finish();
            }
        });
    }

    //not doing anything onstart but needs to be overridden
    @Override
    public void onStart() {
        super.onStart();
    }

    //Email validation regex function made with guidance from
    //https://www.tutorialspoint.com/validate-email-address-in-java
    static boolean IsEmailValid(String emailToCheck) {
        String regularExpression = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return emailToCheck.matches(regularExpression);
    }

    //this method attempts to log the user in
    //will check if the user email entered contains .ac.uk and is a valid email address
    //then check if the password is greater in length than 7  characters before using firebase auth
    //to login the user using their details
    public void DoLogin(String e, String p){
        if(e.contains(".ac.uk") && IsEmailValid(e) && p.length() > 7) {
        authInstance.signInWithEmailAndPassword(e, p)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = authInstance.getCurrentUser();
                            CheckIfValidUser(user);
                        } else {
                            Toast.makeText(LoginActivity.this, "The details you entered are incorrect.", Toast.LENGTH_SHORT).show();
                            CheckIfValidUser(null);
                        }
                    }
                });
        }else{
            Toast.makeText(LoginActivity.this, "The details you entered are incorrect.", Toast.LENGTH_LONG).show();
        }
    }

    //once the user has logged in I need to check if they are a valid user
    //meaning they used their registration link sent to their email to complete sign-up
    //used so that bots and fake emails cant be used
    //non-verified users can enter app but cant do anything with the app
    private void CheckIfValidUser(FirebaseUser u) {
        if (u != null) {
            // Check if user's email is verified
            boolean emailVerified = u.isEmailVerified();

            if(emailVerified) {
                final Intent mainIntent = new Intent(this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }else {
                Toast.makeText(LoginActivity.this, "Check your email for a verification", Toast.LENGTH_SHORT).show();
            }
        } else {
            System.out.println("The user was null");
        }
    }
}
