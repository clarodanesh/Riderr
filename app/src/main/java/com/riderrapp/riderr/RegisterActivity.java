package com.riderrapp.riderr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        /*if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);*/
        getWindow().getDecorView().setBackgroundColor(getColor(R.color.colorPrimaryDark));
        mAuth = FirebaseAuth.getInstance();
        final EditText emailEdit =  (EditText) findViewById(R.id.emailTxtBoxRegister);
        final EditText passwordEdit =  (EditText) findViewById(R.id.passwordTextBoxRegister);
        final Button registerBtn = (Button) findViewById(R.id.RegisterBtnRegister);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String email = (String) emailEdit.getText().toString();
                String password = (String) passwordEdit.getText().toString();

                email = email.replace(" ", "");
                password = password.replace(" ", "");

                register(email, password);
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

    public void register(String e, String p){
        mAuth.createUserWithEmailAndPassword(e, p)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Log.d(TAG, "createUserWithEmail:success");
                            System.out.println("task success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            System.out.println("task fail");
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            System.out.println("user NOT null");
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                //Log.d(TAG, "Email sent.");
                                Toast.makeText(RegisterActivity.this, "Check email for verification",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            System.out.println("user IS null");
        }
    }
}
