package com.tapper.tapcounter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class MainActivity extends AppCompatActivity {
    private int tapCount = 0;
    private TextView tapCountTextView;
    private RelativeLayout backgroundLayout;
    private Button vibrationControlButton;
    private GestureDetector gestureDetector;

    private MediaPlayer resetSound;
    private Vibrator vibrator;

    private boolean isVibrating = false; // Track vibration state

    private Handler tapHandler = new Handler();
    private ObjectAnimator fadeInAnimator;

    private AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tapCountTextView = findViewById(R.id.counterView);
        backgroundLayout = findViewById(R.id.back);
        vibrationControlButton = findViewById(R.id.vibrationControlButton);


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Set up gesture detector to handle single taps
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                handleSingleTap();
                return true;
            }
        });

        View rootView = findViewById(android.R.id.content);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Pass touch events to the gesture detector
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        // Reset button
        Button resetButton = findViewById(R.id.resetTapButton);

        // Initialize animations
        final Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        final Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fadeInAnimator = ObjectAnimator.ofFloat(tapCountTextView, "alpha", 0f, 1f);
        fadeInAnimator.setDuration(300);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tapCount = 0;
                updateCountsSmooth();
                // Vibrate for 200 milliseconds when reset button is clicked
                if (isVibrating) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrate(200);
                    }
                }
                // Play sound for reset
                playResetSound();
                // Set background to default
                backgroundLayout.setBackgroundResource(R.drawable.earth2);
                // Set text color to default (black)
                tapCountTextView.setTextColor(Color.WHITE);
            }
        });

        // Vibration Control button
        vibrationControlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleVibration(); // Add a method to handle the vibration control
            }
        });

    }

    private void handleSingleTap() {
        tapCount++;
        updateCountsSmooth();
        // Change background after every 50 taps
        if (tapCount % 50 == 0) {
            changeBackground();
        }
        // Vibrate for 200 milliseconds on each tap
        if (isVibrating) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrate(200);
            }
        }
    }

    private void updateCounts() {
        tapCountTextView.setText(String.valueOf(tapCount));
    }

    private void updateCountsSmooth() {
        // Fade-out animation for the current count
        final ObjectAnimator fadeOutAnimator = ObjectAnimator.ofFloat(tapCountTextView, "alpha", 1f, 0f);
        fadeOutAnimator.setDuration(300);
        fadeOutAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Update the tap count and fade-in the new count
                updateCounts();
                fadeInAnimator.start();
            }
        });

        // Start the fade-out animation
        fadeOutAnimator.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void vibrate(long milliseconds) {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    private void playResetSound() {
        if (resetSound != null) {
            resetSound.start();
        }
    }

    private void changeBackground() {
        // Change the background image and text color based on tap count
        if (tapCount % 200 == 0) {
            backgroundLayout.setBackgroundResource(R.drawable.moon2);
            tapCountTextView.setTextColor(Color.BLACK);
        } else if (tapCount % 100 == 0) {
            backgroundLayout.setBackgroundResource(R.drawable.mars2);
            tapCountTextView.setTextColor(Color.YELLOW);
        } else {
            backgroundLayout.setBackgroundResource(R.drawable.earth2);
            tapCountTextView.setTextColor(Color.WHITE);
        }
    }

    private void toggleVibration() {
        isVibrating = !isVibrating; // Toggle vibration state
    }

    @Override
    protected void onDestroy() {
        // Release resources when the activity is destroyed
        if (vibrator != null) {
            vibrator.cancel();
        }
        if (resetSound != null) {
            resetSound.release();
        }
        super.onDestroy();
    }
}
