package com.example.visitorz;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import dmax.dialog.SpotsDialog;
import io.opencensus.internal.StringUtils;

public class OldUser extends AppCompatActivity {

    String number, fname, lname, email;
    TextView oldUserBack, lastVisit;
    FirebaseFirestore mFireStore;
    ArrayList<String> arrlist;
    SimpleDateFormat myFormat;
    DateTimeFormatter formatter;
    FirebaseFirestore mFirestore;
    StorageReference storage;
    AlertDialog alertDialog;
    ConstraintLayout mainLayout;
    Handler handler;
    Runnable r;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old_user);
        //remove the notification bar, when video plays
        setTheme(android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Intent intent = getIntent();
        number = intent.getStringExtra("mobile");
        fname = intent.getStringExtra("fname");
        lname = intent.getStringExtra("lname");
        email = intent.getStringExtra("email");

        oldUserBack = findViewById(R.id.oldUserBack);
        lastVisit = findViewById(R.id.lastVisit);
        mainLayout = findViewById(R.id.mainLayout);
        alertDialog = new SpotsDialog.Builder().setContext(OldUser.this).setCancelable(false).build();

        oldUserBack.setText(oldUserBack.getText().toString() + " " + fname.substring(0, 1).toUpperCase() + fname.substring(1) + "!");
        fetchUserImage();

        formatter = DateTimeFormatter.ofPattern("dd_MM_yyyy");
        fetch_dates(number);

        //Handler for redirection
        handler = new Handler();
        r = new Runnable() {
            @Override
            public void run() {
                //do nothing
                Intent back = new Intent(OldUser.this, LauncherActivity.class);
                startActivity(back);
                overridePendingTransition(0,0);
            }
        };
    }

    private void fetchUserImage() {
        storage = FirebaseStorage.getInstance().getReference();
    }

    public void fetch_dates(String number) {
        alertDialog.show();
        arrlist = new ArrayList<>();
        mFireStore = FirebaseFirestore.getInstance();
        mFireStore.collection(number).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
//                        if(!document.getId().equals(todayDate))
                        arrlist.add(document.getId());
//                        Toast.makeText(OldUser.this, document.getId(), Toast.LENGTH_SHORT).show();
                    }

                    try {
                        check_dates(arrlist);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(OldUser.this, "Fetch Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void check_dates(ArrayList<String> datelist) throws ParseException {
        alertDialog.dismiss();
        int min = Integer.MAX_VALUE;
        String previousDate = null;

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy");
        String todayDate = simpleDateFormat.format(cal.getTime());
        for (String date : datelist) {
            final LocalDate dateBefore = LocalDate.parse(date, formatter);
            final LocalDate dateAfter = LocalDate.parse(todayDate, formatter);
            final int noOfDaysBetween = (int) ChronoUnit.DAYS.between(dateBefore, dateAfter);
            if (noOfDaysBetween < min) {
                min = noOfDaysBetween;
                previousDate = date;
            }
        }

        int weeks = min / 7;
        int months = weeks / 4;
        if (weeks < 4) {
            if (weeks < 1) {
                int days = min % 7;
                if (days == 0)
                    lastVisit.setVisibility(View.GONE);
                else if (days == 1) {
                    lastVisit.setVisibility(View.VISIBLE);
                    lastVisit.setText("We're glad to have you back again today!");
                } else {
                    lastVisit.setVisibility(View.VISIBLE);
                    lastVisit.setText("It's been " + days + " days since you visited us.");
                }

            } else {
                lastVisit.setVisibility(View.VISIBLE);
                if (weeks == 1) {
                    lastVisit.setText("It's been a week since you visited us.");
                } else {
                    lastVisit.setText("It's been " + weeks + " weeks since you visited us.");
                }
            }
        } else if (weeks > 4) {
            lastVisit.setVisibility(View.VISIBLE);
            lastVisit.setText("It's been " + months + " months since you visited us.");
        } else if (months > 5) {
            lastVisit.setVisibility(View.VISIBLE);
            lastVisit.setText("It's been so long since you visited us.");
        } else {
            lastVisit.setText("");
        }
        writeToFirestore();
    }

    private void writeToFirestore() {
        mFirestore = FirebaseFirestore.getInstance();

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd_MM_yyyy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HHmm");
        final String dateDoc = simpleDateFormat.format(cal.getTime());
        String timeCol = fname.trim() + timeFormat.format(cal.getTime());

        SimpleDateFormat timeFormat2 = new SimpleDateFormat("HH:mm");
        final Map<String, Object> user = new HashMap<>();
        user.put("first_name", fname);
        user.put("last_name", lname);
        user.put("mobile", number);
        user.put("email", email);
        user.put("visit_date", simpleDateFormat.format(cal.getTime()));
        user.put("visit_time", timeFormat2.format(cal.getTime()));

        //Need to add some dummy data to data document, otherwise can't iterate using queryDocument
        //which we use to check when the visitor last visited us.
        final Map<String, String> dummyData = new HashMap<>();
        dummyData.put("dummyInfo",fname);
        mFireStore.collection(number)
                .document(dateDoc)
                .set(dummyData)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(OldUser.this, "Error: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        mFirestore      //visitors -> 02_07_2019 -> 0610-1131 -> details -> fname,lname,mobile..
            .collection(number)
            .document(dateDoc)
            .collection(timeCol)
            .document("details")
            .set(user)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    backToMain();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(OldUser.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
    }

    private void backToMain() {
        handler.postDelayed(r, 10*1000);          //wait for 30seconds, then redirect to launcher page
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handler.removeCallbacks(r);
    }
}
