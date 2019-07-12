package com.example.visitorz;

import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

import com.example.visitorz.Adapter.SlideShowImageAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import me.relex.circleindicator.CircleIndicator;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class IdleImages extends AppCompatActivity {
    private static ViewPager mPager;
    private static int currentPage = 0;
    private String[] images;
    private ArrayList<String> imageArray = new ArrayList<>();
    Handler handler;
    Runnable Update;

    Timer swipeTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idle_images);
        //remove the notification bar, when video plays
        setTheme(android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //Fetch all images in the defaults folder (i.e <device>/Downloads/IdleResources/Images)
        String path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)+"/IdleResources/Images";
        File directory = new File(path);
        File[] files = directory.listFiles();
        images = new String[files.length];
        for(int i=0; i<files.length;i++) {
            images[i] = path+"/"+files[i].getName();
        }                                                                       //now, array 'images' has the URI paths for all images
        init();
    }

    private void init() {
//        for (int i = 0; i < images.length; i++)
//            imageArray.add(images[i]);
        imageArray.addAll(Arrays.asList(images));                              //add the image paths (int values) to an integer array

        mPager = findViewById(R.id.pager);
        mPager.setAdapter(new SlideShowImageAdapter(IdleImages.this, imageArray));
        CircleIndicator indicator = findViewById(R.id.indicator);
        indicator.setViewPager(mPager);

        // Auto start of viewpager
        handler = new Handler();
        Update = new Runnable() {
            public void run() {
                if (currentPage == images.length) {                            //if current image is the last one, switch to the first one
                    currentPage = 0;                                           //so that the images loop around
                }
                mPager.setCurrentItem(currentPage++, true);         //change to next image
//                mPager.setTranslationX(-1*mPager.getWidth());
//                mPager.setAlpha(1-Math.abs(1));
            }
        };
        swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, 1000, 10*1000);            //images change/swipe with 1second delay and stay on for a period of 10seconds
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        swipeTimer.cancel();
        Intent backToMain = new Intent(IdleImages.this,LauncherActivity.class);
        startActivity(backToMain);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

}