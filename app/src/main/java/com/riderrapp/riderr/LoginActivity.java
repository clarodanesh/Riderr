package com.riderrapp.riderr;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class LoginActivity extends AppCompatActivity {

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
            }
        });
    }
}
