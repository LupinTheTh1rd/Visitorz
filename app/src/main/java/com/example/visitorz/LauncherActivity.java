package com.example.visitorz;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import java.io.File;


import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class LauncherActivity extends AppCompatActivity {

    Button bsms;
    EditText mobile;
    String sms;
    int SMS_PERMISSION_CODE = 1;

    Handler handler;
    Runnable r;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        //remove the notification bar
        setTheme(android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mobile = findViewById(R.id.mobile);
        mobile.setText("");
        bsms = findViewById(R.id.next);

        sms = "A guest has arrived to meet you. Please be there at the reception.";

        bsms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
                    } else {
                        sendSMS(sms);
                    }
                }
            }
        });

        handler = new Handler();
        r = new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                showIdleResource();
            }
        };

        if(mobile.getText().toString().isEmpty())   //if empty, then we check if idle for long
            startHandler();

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(mobile.getText().toString().isEmpty())   //if empty, then we check if idle for long
                    startHandler();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(mobile.getText().toString().isEmpty())   //if empty, then we check if idle for long
                    startHandler();
                else            //if not empty, user is entering number
                    stopHandler();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        mobile.addTextChangedListener(textWatcher);
        //NEW
        //close keypad when user touches anywhere on screen (except keypad), more like when the focus changes from editText
        mobile.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        //NEW
        //proceed to next page, via enter key on keypad as well
        mobile.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    sendSMS(sms);
                    return true;
                }
                return false;
            }
        });
    }

    private void showIdleResource() {
        String path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS) + "/IdleResources/Videos";
        File directory = new File(path);
        File[] files = directory.listFiles();
        Intent imageIntent;
        if (files.length >= 1) {                 //if the Video folder has any video in it, play the video, instead of images {higher priority}
            imageIntent = new Intent(LauncherActivity.this, IdleVideos.class);
        } else {                                //if no video, show images
            imageIntent = new Intent(LauncherActivity.this, IdleImages.class);
        }
        startActivity(imageIntent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        startHandler();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "PERMISSION GRANTED!", Toast.LENGTH_SHORT).show();
                sendSMS(sms);
            } else {
                Toast.makeText(this, "PERMISSION DENIED!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SMS_PERMISSION_CODE
                && resultCode == LauncherActivity.RESULT_OK) {
            sendSMS(sms);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopHandler();
    }

    private void sendSMS(String sms) {
        if(mobile.getText().toString().length() != 10){
            Toast.makeText(LauncherActivity.this, "Please enter a valid number!", Toast.LENGTH_LONG).show();
            mobile.setText("");
            mobile.requestFocus();
        }
        else {
            Intent i = new Intent(LauncherActivity.this, MainActivity.class);
            i.putExtra("number",mobile.getText().toString());
            startActivity(i);
            overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        }
    }

    public void stopHandler() {
        handler.removeCallbacks(r);
    }

    public void startHandler() {
        handler.postDelayed(r, 2*60* 1000);   //for 2 minutes
        //so, when the tablet is not interacted with for a period of 2mintues, the idle resources will be displayed (videos/images, as set by admin)
    }

    //NEW
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
