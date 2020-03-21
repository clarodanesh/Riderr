package com.riderrapp.riderr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth authInstance;

    @Override
    protected void onCreate(Bundle instanceState) {
        super.onCreate(instanceState);
        setContentView(R.layout.activity_splash);
        getWindow().getDecorView().setBackgroundColor(getColor(R.color.colorPrimaryDark));
        if(Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(getColor(R.color.colorPrimaryDark));
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        FirebaseUser currUser = authInstance.getInstance().getCurrentUser();
                        CheckUser(currUser);
                    }
                },
                500);
    }

    private void CheckUser(FirebaseUser user){
        if(user != null && user.isEmailVerified() == true){
            final Intent mainIntent = new Intent(this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        }
        else{
            final Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }
    }
}
