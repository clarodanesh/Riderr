package com.riderrapp.riderr;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    //member variables for the splashactivity class
    private FirebaseAuth authInstance;

    @Override
    protected void onCreate(Bundle instanceState) {
        super.onCreate(instanceState);
        setContentView(R.layout.activity_splash);
        //need to set the background color on create
        getWindow().getDecorView().setBackgroundColor(getColor(R.color.colorPrimaryDark));
        //check if the android sdk ver is 21 or above before setting nav color
        if(Build.VERSION.SDK_INT >= 21) {
            getWindow().setNavigationBarColor(getColor(R.color.colorPrimaryDark));
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        //once the activity has started I need to delay it so user will see the logo
        //need to get the user too and check if need to nav to mainactivity or login activity
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        FirebaseUser currUser = authInstance.getInstance().getCurrentUser();
                        CheckUser(currUser);
                    }
                },
                500);
    }

    //check the user to see if they are verified and not null
    //if they are not verified and not null then go to main activity
    //otherwise send user to login activity
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
