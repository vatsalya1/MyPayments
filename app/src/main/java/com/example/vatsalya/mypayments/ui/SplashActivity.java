package com.example.vatsalya.mypayments.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vatsalya.mypayments.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by vatsalya on 12/28/16.
 */


public class SplashActivity extends Activity {
    private static final String TAG = "SplashActivity";
    private final static String USER_ID = "UserID";
    Thread splashTread;
    private Context context;
    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    // [END declare_auth]

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    // [END declare_auth_listener]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        context = this;
        sharedPreferences = context.getSharedPreferences(
                getString(R.string.preference_file_key), context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
        // setupWindowAnimations();


        mAuth=FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        StartAnimations(user);
        // [END initialize_auth]

    }
    private void setupWindowAnimations() {
        // Re-enter transition is executed when returning to this activity
        Slide slideTransition = new Slide();
        slideTransition.setSlideEdge(Gravity.LEFT);
        slideTransition.setDuration(getResources().getInteger(R.integer.anim_duration_long));
        getWindow().setReenterTransition(slideTransition);
        getWindow().setExitTransition(slideTransition);
    }

    private void StartAnimations(final FirebaseUser user) {
        TextView tv=(TextView) findViewById(R.id.text_logo);
        ImageView iv = (ImageView) findViewById(R.id.image_logo);

        Animation anim = AnimationUtils.loadAnimation(this, R.anim.alpha);
        anim.reset();
        tv.clearAnimation();
        tv.startAnimation(anim);

        Animation animRotate = AnimationUtils.loadAnimation(this, R.anim.alpha_rotate);
        anim.reset();
        iv.clearAnimation();
        iv.startAnimation(animRotate);

        splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    // Splash screen pause time
                    while (waited < 5000) {
                        sleep(100);
                        waited += 100;
                    }
                    if (user == null){
                    Intent intent = new Intent(SplashActivity.this,
                            LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    } else if (user != null){
                        String userID = user.getUid();
                        context = SplashActivity.this;
                        sharedPreferences = context.getSharedPreferences(
                                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
                        editor = sharedPreferences.edit();

                        editor.putString(getString(R.string.logged_in_user_id), userID);
                        editor.commit();
                        Intent i = new Intent(SplashActivity.this, MainActivity.class);
                        i.putExtra(USER_ID, userID);
                        startActivity(i);
                    }


                    SplashActivity.this.finish();
                }catch (InterruptedException e) {
// do nothing
                } finally {
                    finish();
                }
            }
        };
        splashTread.start();
    }
}
