package com.example.tracknowdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tracknowdemo.ui.profile.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {

    private static int SPLASH_SCREEN=2500;
    Animation topAnim,bottomAnim;
    ImageView image;
    TextView logo,text;
    private FirebaseAuth auth;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();

//        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);
        image=findViewById(R.id.imageView);
        logo=findViewById(R.id.textView);
        text=findViewById(R.id.textView2);

        //Animation
        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_animation);
        bottomAnim = AnimationUtils.loadAnimation(this,R.anim.bottom_animation);

        image.setAnimation(topAnim);
        logo.setAnimation(bottomAnim);
        text.setAnimation(bottomAnim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(auth.getCurrentUser()!=null){
                    intent = new Intent(SplashScreen.this, DashboardMain.class);
                    startActivity(intent);
                }else{
                    intent=new Intent(SplashScreen.this, LoginActivity.class);
                    Pair[] pairs =new Pair[2];
                    pairs[0]=new Pair<View,String>(image,"logo_image");
                    pairs[1]=new Pair<View,String>(logo,"logo_text");
                    ActivityOptions options=ActivityOptions.makeSceneTransitionAnimation(SplashScreen.this,pairs);
                    startActivity(intent,options.toBundle());
                }
                finish();
            }
        },SPLASH_SCREEN);
    }
}