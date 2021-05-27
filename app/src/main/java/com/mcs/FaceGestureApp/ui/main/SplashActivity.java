package com.mcs.FaceGestureApp.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.mcs.FaceGestureApp.R;
import com.mcs.FaceGestureApp.ui.auth.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        final TextView txt1=findViewById(R.id.textView10);


        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 0.7f);
        alphaAnimation.setDuration(2000);
        txt1.startAnimation(alphaAnimation);

        //hold screen for 3 to 5 seconds and move to main screen
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //go to next activity
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
        }, 3000); // for 3 second
    }



}
