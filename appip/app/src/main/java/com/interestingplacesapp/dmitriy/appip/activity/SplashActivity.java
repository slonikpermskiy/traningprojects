package com.interestingplacesapp.dmitriy.appip.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.interestingplacesapp.dmitriy.appip.R;

public class SplashActivity extends AppCompatActivity {

    ImageView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        view = (ImageView) findViewById(R.id.imageView);
        RotateAnimation anim = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        //Setup anim with desired properties
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE); //Repeat animation indefinitely
        anim.setDuration(3000); //Put desired duration per anim cycle here, in milliseconds
        //Start animation
        view.startAnimation(anim);

        Thread t=new Thread() {
            public void run() {
                try {
                    //sleep thread for 10 seconds, time in milliseconds
                    sleep(2000);

                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        //start thread
        t.start();
    }
}


