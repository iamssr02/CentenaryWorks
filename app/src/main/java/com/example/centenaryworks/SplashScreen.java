package com.example.centenaryworks;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        long delayMillis = 1000;

        // Use a Handler to post a delayed action
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start your activity here
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    // User is signed in
                    // Start home activity
                    startActivity(new Intent(SplashScreen.this, MainActivity.class));
                } else {
                    // No user is signed in
                    // Start login activity
                    startActivity(new Intent(SplashScreen.this, CreateAccountActivity.class));
                }
                finish();
            }
        }, delayMillis);

    }
}