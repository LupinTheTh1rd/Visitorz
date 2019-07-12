package com.example.visitorz;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener{

//    EditText first_name, last_name, email, mobile;
    //NEW
    EditText last_name, email, mobile;
    AutoCompleteTextView first_name;
    private static final String[] PREFIX = new String[] {
        "Mr. ", "Mrs. ", "Ms. "
    };
    TextInputLayout name1, name2, optional_mail, mob_number;
    Button proceed1;
//    Button proceed2;
    ConstraintLayout layout,main;

    String fname, lname, mail = "", number;
    StorageReference storageReference;
    FirebaseFirestore mFirestore;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //remove the notification bar
        setTheme(android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        proceed1 = findViewById(R.id.next);
//        proceed2 = findViewById(R.id.next_page);
        first_name = findViewById(R.id.fname);
        last_name = findViewById(R.id.lname);
        email = findViewById(R.id.email);
        mobile = findViewById(R.id.mobile);

        layout = findViewById(R.id.floating_layout);
        main = findViewById(R.id.main_layout);

        proceed1.setOnClickListener(this);
//        proceed2.setOnClickListener(this);
        //NEW
        setPrefix();        //set the Gender prefix for first name editText

        TextWatcher checkForChange = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                fname = first_name.getText().toString();
                lname = last_name.getText().toString();
                number = mobile.getText().toString();
//                String mail = email.getText().toString();     //don't need to watch for email, since it is optional
//                if(!fname.isEmpty() && !lname.isEmpty() && !number.isEmpty() && !mail.isEmpty()) {
                if (!fname.isEmpty() && !lname.isEmpty() && !number.isEmpty()) {
                    proceed1.setEnabled(true);
//                    proceed2.setEnabled(true);
                } else {
                    proceed1.setEnabled(false);
//                    proceed2.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        first_name.addTextChangedListener(checkForChange);
        last_name.addTextChangedListener(checkForChange);
        mobile.addTextChangedListener(checkForChange);
//        email.addTextChangedListener(checkForChange);

        alertDialog = new SpotsDialog.Builder().setContext(MainActivity.this).setCancelable(false).build();

        //NEW
        //close keypad when user touches anywhere on screen (except keypad), more like when the focus changes from editText
        last_name.setOnFocusChangeListener(this);
        mobile.setOnFocusChangeListener(this);
        email.setOnFocusChangeListener(this);
        //little extra feature for first name editText, when in focus; show the dropdown for gender prefix
        first_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    first_name.showDropDown();
                else
                    hideKeyboard(v);
            }
        });
    }

    @Override
    public void onClick(View v) {

        if(mobile.getText().toString().length() != 10) {
            Toast.makeText(this, "Please enter a valid mobile number!", Toast.LENGTH_SHORT).show();
            mobile.setText("");
            mobile.requestFocus();
        } else {
            //send sms
            Intent i = getIntent();
            String num = i.getStringExtra("number");
            String message="";
            message = first_name.getText().toString()+" "+last_name.getText().toString()+"\nMobile number: "+mobile.getText().toString()+", has arrived to meet you. Please be there at the reception.";
            try {
                SmsManager smgr = SmsManager.getDefault();
                smgr.sendTextMessage(num, null, message, null, null);
                Toast.makeText(MainActivity.this, "The visitee will receive you shortly.", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "SMS Failed to Send, Please try again", Toast.LENGTH_SHORT).show();
            }

            alertDialog.show();
            storageReference = FirebaseStorage.getInstance().getReference();

            mail = email.getText().toString().isEmpty()
                    ? ""
                    : email.getText().toString();


            storageReference.child("images/" + number + "/photo")
                    .getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Got the download URL for 'users/me/profile.png'
                            alertDialog.dismiss();
//                            Toast.makeText(MainActivity.this, "Old user!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, OldUser.class);
                            intent.putExtra("fname", fname);
                            intent.putExtra("mobile", number);
                            intent.putExtra("lname", lname);
                            intent.putExtra("email", mail);
                            startActivity(intent);
                            overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // File not found
                    //send to already exists user page
                    alertDialog.dismiss();
                    Intent intent = new Intent(MainActivity.this, ImageActivity.class);
                    intent.putExtra("number", number);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                }
            });
        }
    }

    //NEW
    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //NEW
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            hideKeyboard(v);
        }
    }

    public void setPrefix() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, PREFIX);
        first_name.setAdapter(adapter);
    }
}
