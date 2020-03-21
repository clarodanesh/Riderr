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

    private FirebaseAuth authInstance;

    @Override
    protected void onCreate(Bundle instanceState) {
        super.onCreate(instanceState);
        setContentView(R.layout.activity_login);

        getWindow().getDecorView().setBackgroundColor(getColor(R.color.colorPrimaryDark));
        if(Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(getColor(R.color.colorPrimaryDark));
        }

        authInstance = FirebaseAuth.getInstance();
        final EditText emailEdit =  (EditText) findViewById(R.id.emailTxtBoxLogin);
        final EditText passwordEdit =  (EditText) findViewById(R.id.passwordTextBoxLogin);
        final Button loginBtn = (Button) findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String email = (String) emailEdit.getText().toString();
                String password = (String) passwordEdit.getText().toString();

                email = email.replace(" ", "");
                password = password.replace(" ", "");

                DoLogin(email, password);
            }
        });

        final Intent registerIntent = new Intent(this, RegisterActivity.class);
        final Button registerLoginBtn = (Button) findViewById(R.id.registerBtnLogin);
        registerLoginBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(registerIntent);
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

    public void DoLogin(String e, String p){
        if(/*e.contains(".ac.uk") &&*/ IsEmailValid(e) && p.length() > 7) {
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
