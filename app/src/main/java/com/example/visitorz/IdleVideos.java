package com.example.visitorz;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class IdleVideos extends AppCompatActivity {

    VideoView video;
    private int READ_STORAGE_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idle_videos);
        //remove the notification bar, when video plays
        setTheme(android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        video = findViewById(R.id.idleVideo);
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, READ_STORAGE_PERMISSION_CODE);
            } else {
                init();
            }
        }
    }

    private void init() {
        String path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS) + "/IdleResources/Videos";
        File directory = new File(path);
        File[] files = directory.listFiles();
        if (files.length > 1) {                  //if more than one video existing in folder, show error!
            Toast.makeText(this, files[0].getName()+"", Toast.LENGTH_SHORT).show();
            android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(IdleVideos.this);
            alertDialog.setTitle("Error!");
            alertDialog.setMessage("Failed to parse video!\nMultiple Files found at given location.");
            alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent backToMain = new Intent(IdleVideos.this, LauncherActivity.class);
                    startActivity(backToMain);
                    overridePendingTransition(0, 0);
                }
            });
            alertDialog.create();
            alertDialog.show();
        } else {                                  //if only one video, play it, and loop once it ends(until any user interaction)
            String videoPath = path + "/" + files[0].getName();
            Uri uri = Uri.parse(videoPath);
            video.setVideoURI(uri);
            video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                    video.start();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_STORAGE_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                init();
            } else {
                Toast.makeText(this, "Can't fetch videos to run!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void backToMain(View view) {
        video.stopPlayback();
        Intent backToMain = new Intent(IdleVideos.this, LauncherActivity.class);
        startActivity(backToMain);
        overridePendingTransition(0, 0);
    }
}
