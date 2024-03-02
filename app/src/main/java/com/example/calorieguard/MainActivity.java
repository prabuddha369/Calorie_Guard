package com.example.calorieguard;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkQuery;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onUserLeaveHint() {
        footer.Stop();
        super.onUserLeaveHint();
    }

    private boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (searchView.hasFocus()) {
            // If the searchView has focus, clear the focus and restore its original position and width

            // Clear the focus to hide the soft keyboard
            searchView.clearFocus();
            if (searchView.getText().toString().isEmpty()) {
                Calories = "";
                type = "";
            }

        } else if (fd_wd.hasFocus()) {
            if(!fd_wd.getText().toString().contains("g")) {
               String fd_wt_with_g = fd_wd.getText().toString()+" g";
               fd_wd.setText(fd_wt_with_g);
            }
            // Clear the focus to hide the soft keyboard
            fd_wd.clearFocus();
        } else {
            if (doubleBackToExitPressedOnce) {
                footer.Stop();
                super.onBackPressed();
                finishAffinity(); // Close the app
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press Back again to Exit.", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        }
    }

    public TextView Lweight, Gweight, CurrCal, User, W, A, H, buttonsignout, privacy_policy, About_us, profile, TandC, seelistB, seelistD, seelistL, seeListS, breakfastCal, lunchCal, dinnerCal, snaksCal;
    public Button additem;
    public AutoCompleteTextView searchView, fd_wd;
    public View viewbg;
    private ImageView cross, addB, addL, addD, addS, userDp;
    private ProgressBar progressBar;
    private Handler handler;
    private int currentTextIndex = 0;
    private String[] textArray = {"Search Veggies", "Search Fruits", "Search Meals", "Search Foods", "Search Snacks"};
    public int wt_fd;
    private String email;
    private ArrayList<String> foodNamesList = new ArrayList<>();
    private Footer footer;
    String str, dpUrl, type = "", Calories = "";
    private LinearLayout popupView, searchfield, MenuButton;
    private boolean isPopupVisible = false;
    int i = 2;

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).hide();

        Lweight = findViewById(R.id.Lweight);
        Gweight = findViewById(R.id.Gweight);
        CurrCal = findViewById(R.id.CurrCal);
        User = findViewById(R.id.username);
        userDp = findViewById(R.id.userdp);
        buttonsignout = findViewById(R.id.buttonsignout);
        W = findViewById(R.id.HW);
        A = findViewById(R.id.agetv);
        H = findViewById(R.id.heighttv);
        viewbg = findViewById(R.id.searchcover);
        MenuButton = findViewById(R.id.menubtn);
        popupView = findViewById(R.id.menuview);
        additem = findViewById(R.id.additem);
        progressBar = findViewById(R.id.progressBar3);
        viewbg.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        searchView = findViewById(R.id.searchView);
        fd_wd = findViewById(R.id.fd_wt);
        searchfield = findViewById(R.id.searchfield);
        privacy_policy = findViewById(R.id.pvp);
        About_us = findViewById(R.id.abtus);
        profile = findViewById(R.id.profile);
        TandC = findViewById(R.id.tandc);
        seelistB = findViewById(R.id.seelistB);
        seelistD = findViewById(R.id.seelistD);
        seelistL = findViewById(R.id.seelistL);
        seeListS = findViewById(R.id.seeListS);
        cross = findViewById(R.id.cross);
        addB = findViewById(R.id.addB);
        addL = findViewById(R.id.addL);
        addD = findViewById(R.id.addD);
        addS = findViewById(R.id.addS);
        breakfastCal = findViewById(R.id.breakfastCal);
        lunchCal = findViewById(R.id.LunchCal);
        dinnerCal = findViewById(R.id.dinnerCal);
        snaksCal = findViewById(R.id.snaksCal);

        createNotificationChannel();

        seelistB.setPaintFlags(seelistB.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        seelistL.setPaintFlags(seelistL.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        seelistD.setPaintFlags(seelistD.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        seeListS.setPaintFlags(seelistD.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        if (!areNotificationPermissionsGranted()) {
            requestNotificationPermissions();
        }

        DBHelper dbHelper = new DBHelper(this);
        dbHelper.close();

        handler = new Handler(Looper.getMainLooper());
        startTextChangeLoop();

        View rootView = findViewById(android.R.id.content);

        // Get the email passed from the previous activity and set up the Firebase database reference
        email = getIntent().getExtras().getString("Email");

        // Load the food names from the CSV file and get User Data and set the current activity footer background
        loadFoodNamesFromCSV();
        Get_User_Data(email);
        progressBar.setVisibility(View.GONE);
        footer = new Footer(this, rootView, email);
        footer.setDashboardBackground(R.drawable.circlebg);


        NumberRangeAdapter adapterN = new NumberRangeAdapter(this);
        fd_wd.setAdapter(adapterN);
        // Set the drop-down anchor to display the suggestions above the view
        fd_wd.setDropDownAnchor(R.id.fd_wt);

        fd_wd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!fd_wd.getText().toString().contains("g")) {
                        fd_wd.setText(fd_wd.getText().toString() + " g");
                    }
                    wt_fd = getFoodWeightValue();
                    fd_wd.clearFocus();

                    viewbg.setVisibility(View.GONE);
                    additem.setVisibility(View.VISIBLE);

                    searchView.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

                    return true;
                }
                return false;
            }
        });


        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, foodNamesList);
        searchView.setAdapter(adapter);

        fd_wd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Change the width to match_parent
                    viewbg.setVisibility(View.VISIBLE);
                    additem.setVisibility(View.GONE);
                    ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) searchfield.getLayoutParams();
//                     Move the searchView to the top of searchcover
                    layoutParams.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
                    layoutParams.topToTop = R.id.searchcover;
                    layoutParams.topMargin = 200;
                    searchfield.setLayoutParams(layoutParams);

                } else {
                    // When the searchView loses focus (soft keyboard closed), restore its original position and width
                    viewbg.setVisibility(View.GONE);
                    additem.setVisibility(View.VISIBLE);
                    // Restore the original width and position of the searchView
                    ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) searchfield.getLayoutParams();
                    layoutParams.topToTop = ConstraintLayout.LayoutParams.UNSET;
                    layoutParams.bottomToTop = R.id.additem;
                    searchfield.setLayoutParams(layoutParams);
                }
            }
        });

        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Change the width to match_parent
                    viewbg.setVisibility(View.VISIBLE);
                    additem.setVisibility(View.GONE);
                    ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) searchfield.getLayoutParams();
//                     Move the searchView to the top of searchcover

                    layoutParams.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
                    layoutParams.topToTop = R.id.searchcover;
                    layoutParams.topMargin = 200;
                    searchfield.setLayoutParams(layoutParams);

                    searchView.showDropDown();
                } else {
                    // When the searchView loses focus (soft keyboard closed), restore its original position and width
                    viewbg.setVisibility(View.GONE);
                    additem.setVisibility(View.VISIBLE);
                    // Restore the original width and position of the searchView
                    ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) searchfield.getLayoutParams();
                    layoutParams.topToTop = ConstraintLayout.LayoutParams.UNSET;
                    layoutParams.bottomToTop = R.id.additem;
                    searchfield.setLayoutParams(layoutParams);
                }
            }
        });

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used in this implementation
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not used in this implementation
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Get the filterable adapter and apply the filter to it
                Filterable adapter = (Filterable) searchView.getAdapter();
                adapter.getFilter().filter(s);
            }
        });


        searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // Get the search query and hide the keyboard
                    fd_wd.setText("100 g");
                    fd_wd.requestFocus();
                    return true;
                }
                return false;
            }
        });

        buttonsignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSignoutConfirmationDialog();
            }
        });

        additem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(additem, "alpha", 0.5f, 2.0f);
                alphaAnimator.setDuration(1000); // 1000 milliseconds = 1 second
                alphaAnimator.start();

                if (searchView.getText().toString().isEmpty() && fd_wd.getText().toString().isEmpty()) {
                    // Vibrate the device
                    vibrateDevice(MainActivity.this);
                    shakeView(searchView);
                    shakeView(fd_wd);
                    return;
                } else if (searchView.getText().toString().isEmpty()) {
                    vibrateDevice(MainActivity.this);
                    shakeView(searchView);
                    return;
                } else if (fd_wd.getText().toString().isEmpty()) {
                    vibrateDevice(MainActivity.this);
                    shakeView(fd_wd);
                    return;
                }
                else if (!foodCaloriesExist(searchView.getText().toString())) {
                    vibrateDevice(MainActivity.this);
                    shakeView(searchView);
                    return;
                }

                str = searchView.getText().toString();
                String wt = fd_wd.getText().toString();

                if (type.isEmpty()) {
                    showCustomAlertDialog(str, email, wt);
                } else {
                    Intent intent = new Intent(getApplicationContext(), BucketList.class);

                    String CalculatedCal = CalculateCal(getCaloriesFromCSV(str));

                    if (dbHelper.doesItemExist(email, type + " " + str + " " + wt)) {
                        dbHelper.insertItemData(email, type + " " + str + " " + wt + "(" + Integer.toString(i++) + ")", CalculatedCal);
                        intent.putExtra("Email", email);
                        startActivity(intent);
                    } else {
                        dbHelper.insertItemData(email, type + " " + str + " " + wt, CalculatedCal);
                        intent.putExtra("Email", email);
                        startActivity(intent);
                    }
                    searchView.setText("");
                    fd_wd.setText("");
                    type = "";
                }
            }
        });

        HashMap<String, String> itemMap = new HashMap<String, String>();

        itemMap = dbHelper.getAllItemsByUserId(email);
        String fd, cal;
        int sumB = 0, sumL = 0, sumD = 0, sumS = 0;
        if (!itemMap.isEmpty()) {
            for (Map.Entry<String, String> entry : itemMap.entrySet()) {
                fd = entry.getKey();
                if (isBreakfast(fd)) {
                    cal = entry.getValue();
                    sumB += Integer.parseInt(Tocal(cal));
                }
                if (isLunch(fd)) {
                    cal = entry.getValue();
                    sumL += Integer.parseInt(Tocal(cal));
                }
                if (isSnaks(fd)) {
                    cal = entry.getValue();
                    sumS += Integer.parseInt(Tocal(cal));
                }
                if (isDinner(fd)) {
                    cal = entry.getValue();
                    sumD += Integer.parseInt(Tocal(cal));
                }
            }
        }
        breakfastCal.setText(sumB + " cal");
        lunchCal.setText(sumL + " cal");
        dinnerCal.setText(sumD + " cal");
        snaksCal.setText(sumS + " cal");

        MenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePopupView();
            }
        });

        privacy_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String websiteUrl = "https://sites.google.com/view/calorieguardlife/home";

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl));
                startActivity(intent);
            }
        });

        About_us.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String websiteUrl = "https://www.calorieguard.life";

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl));
                startActivity(intent);
            }
        });

        TandC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String websiteUrl = "https://sites.google.com/view/calorie-guard21/home";

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl));
                startActivity(intent);
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglePopupView();
                Intent intent = new Intent(MainActivity.this, Profile.class);
                intent.putExtra("Email", email);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        seelistB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BucketList.class);
                intent.putExtra("Email", email);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this);
                startActivity(intent, options.toBundle());
                overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
            }
        });

        seelistD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BucketList.class);
                intent.putExtra("Email", email);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this);
                startActivity(intent, options.toBundle());
                overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
            }
        });

        seelistL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BucketList.class);
                intent.putExtra("Email", email);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this);
                startActivity(intent, options.toBundle());
                overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
            }
        });

        seeListS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BucketList.class);
                intent.putExtra("Email", email);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(MainActivity.this);
                startActivity(intent, options.toBundle());
                overridePendingTransition(R.anim.fade_out, R.anim.fade_in);
            }
        });

        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                togglePopupView();
            }
        });

        addB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = "B";
                searchView.requestFocus();
            }
        });

        addL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = "L";
                searchView.requestFocus();
            }
        });

        addD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = "D";
                searchView.requestFocus();
            }
        });

        addS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                type = "S";
                searchView.requestFocus();
            }
        });

        scheduleMidNightWork();

    }

    private void scheduleMidNightWork() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        // Set the time to 00:00:00.000
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // If the current time is already past 00:00, schedule it for the next day
        if (System.currentTimeMillis() > calendar.getTimeInMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(MidnightWorker.class)
                .setInitialDelay(calendar.getTimeInMillis() - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .setConstraints(new Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .setRequiresCharging(false)
                        .setRequiresDeviceIdle(false)
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .build())
                .build();

        WorkManager.getInstance(this).enqueue(workRequest);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Use context.getSystemService instead of getSystemService
            NotificationChannel channel = new NotificationChannel("calorieGuard", "CalorieGuardChannel", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Channel for Calorie Guard Notifications");

            NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void togglePopupView() {
        if (isPopupVisible) {
            hidePopupView();
        } else {
            showPopupView();
        }
    }

    private void showPopupView() {
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_in);
        popupView.startAnimation(slideUp);
        popupView.setVisibility(View.VISIBLE);
        isPopupVisible = true;
    }

    private void hidePopupView() {
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_out);
        popupView.startAnimation(slideDown);
        popupView.setVisibility(View.INVISIBLE);
        isPopupVisible = false;
    }

    private void loadFoodNamesFromCSV() {
        try {
            // Open the CSV file from the raw folder
            InputStream inputStream = getResources().openRawResource(R.raw.food_calories);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            // Read each line of the CSV file
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(","); // Split the line using the comma separator

                if (values.length >= 1) {
                    // The first element contains the food name
                    String foodName = values[0].trim();
                    foodNamesList.add(foodName);
                }
            }

            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean foodCaloriesExist(String foodName) {
        return foodNamesList.contains(foodName);
    }

    private String getCaloriesFromCSV(String foodName) {
        try {
            // Open the CSV file from the raw folder
            InputStream inputStream = getResources().openRawResource(R.raw.food_calories);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

            // Read each line of the CSV file
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] values = line.split(","); // Split the line using the comma separator

                if (values.length >= 2) {
                    // The first element contains the food name and the second element contains the calories
                    String currentFoodName = values[0].trim();
                    String calories = values[1].trim();

                    // Check if the currentFoodName matches the given foodName
                    if (currentFoodName.equalsIgnoreCase(foodName)) {
                        // Close the BufferedReader and return the calorie count
                        bufferedReader.close();
                        return calories;
                    }
                }
            }

            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If the foodName is not found in the CSV file, return null or an appropriate value
        return null;
    }

    public void Get_User_Data(String email) {

        DBHelper dbHelper = new DBHelper(this);
        dbHelper.close();

        if (dbHelper.doesUserExist(email)) {
            HashMap<String, String> userData = dbHelper.getUserDataById(email);
            String name = userData.get("name");
            String weight = userData.get("weight");
            String height = userData.get("height");
            double height_val = Double.parseDouble(height);
            int roundedHeight = (int) Math.round(height_val);
            height = String.valueOf(roundedHeight);

            String age = userData.get("age");
            String sex = userData.get("sex");
            String curcal = userData.get("curcal");
            String dpUrl = userData.get("DpUrl");

            Glide.with(MainActivity.this)
                    .load(dpUrl)
                    .apply(new RequestOptions().placeholder(R.drawable.pp).error(R.drawable.pp).transform(new CircleCrop()))
                    .into(userDp);

            assert sex != null;
            if (sex.equals("Male")) {
                int curr_cal_men = Integer.parseInt(curcal);
                CurrCal.setText(Integer.toString(curr_cal_men));
                Lweight.setText(Integer.toString(curr_cal_men - 500));
                Gweight.setText(Integer.toString(curr_cal_men + 500));
                User.setText(firstname(name));
                W.setText(weight + " kg");
                A.setText(age + " years");
                H.setText(height + " cm");
            } else if (sex.equals("Female")) {
                int curr_cal_women = Integer.parseInt(curcal);
                CurrCal.setText(Integer.toString(curr_cal_women));
                Lweight.setText(Integer.toString(curr_cal_women - 500));
                Gweight.setText(Integer.toString(curr_cal_women + 500));
                User.setText(firstname(name));
                W.setText(weight + " kg");
                A.setText(age + " years");
                H.setText(height + " cm");
            }
        }
    }

    public String firstname(String s) {
        String r = "";
        s.trim();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ' ') {
                break;
            } else {
                r += s.charAt(i);
            }
        }
        return r;
    }

    private int getFoodWeightValue() {
        String foodWeightString = fd_wd.getText().toString().trim();

        if (foodWeightString.endsWith("g")) {
            foodWeightString = foodWeightString.substring(0, foodWeightString.length() - 1).trim();
        }

        int foodWeightValue = 0;
        try {
            foodWeightValue = Integer.parseInt(foodWeightString);
        } catch (NumberFormatException e) {
            // Handle the case where the input is not a valid integer
            // You can show an error message or take appropriate action
        }
        return foodWeightValue;
    }

    public String CalculateCal(String caloriesString) {
        // Step 1: Remove " cal" from the end of the caloriesString
        String caloriesValueStr = caloriesString.replaceAll(" cal$", "").trim();

        // Step 2: Convert the caloriesValueStr to a numeric value
        int caloriesValue = 0;
        try {
            caloriesValue = Integer.parseInt(caloriesValueStr);
        } catch (NumberFormatException e) {
            // Handle the case where the input is not a valid integer
            // You can show an error message or take appropriate action
            return "";
        }

        // Step 3: Calculate the final calorie value
        int calculatedCalories = (caloriesValue * wt_fd) / 100;

        // Step 4: Append " cal" at the end of the calculatedCalories and return the result
        return calculatedCalories + " cal";
    }

    public String Tocal(String s) {
        String cal = "";
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == 'c') {
                break;
            } else {
                cal += s.charAt(i);
            }
        }
        return cal.trim();
    }

    public boolean isBreakfast(String s) {
        return s.charAt(0) == 'B';
    }

    public boolean isLunch(String s) {
        return s.charAt(0) == 'L';
    }

    public boolean isDinner(String s) {
        return s.charAt(0) == 'D';
    }

    public boolean isSnaks(String s) {
        return s.charAt(0) == 'S';
    }

    private void showCustomAlertDialog(String f, String email, String wt) {

        DBHelper dbHelper = new DBHelper(this);
        dbHelper.close();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.custom_alert_dialog, null);

        TextView titleTextView = dialogView.findViewById(R.id.dialogTitle);
        String Calculate_cals = CalculateCal(getCaloriesFromCSV(str));
        titleTextView.setText("For which meal you are having " + f + "?\nCalories : " + Calculate_cals);

        Button option1Button = dialogView.findViewById(R.id.option1Button);
        Button option2Button = dialogView.findViewById(R.id.option2Button);
        Button option3Button = dialogView.findViewById(R.id.option3Button);
        Button option4Button = dialogView.findViewById(R.id.option4Button);

        final AlertDialog alertDialog = builder.setView(dialogView).create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Window window = alertDialog.getWindow();
                if (window != null) {
                    window.setBackgroundDrawableResource(R.drawable.custom_alert_bg);
                }
            }
        });
        option1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "B";
                Intent intent = new Intent(getApplicationContext(), BucketList.class);

                String CalculatedCal = CalculateCal(getCaloriesFromCSV(str));

                if (dbHelper.doesItemExist(email, type + " " + str + " " + wt)) {
                    dbHelper.insertItemData(email, type + " " + str + " " + wt + "(" + Integer.toString(i++) + ")", CalculatedCal);
                    intent.putExtra("Email", email);
                    startActivity(intent);
                } else {
                    dbHelper.insertItemData(email, type + " " + str + " " + wt, CalculatedCal);
                    intent.putExtra("Email", email);
                    startActivity(intent);
                }
                searchView.setText("");
                fd_wd.setText("");
                type = "";
                alertDialog.dismiss();
            }
        });

        option2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "L";
                Intent intent = new Intent(getApplicationContext(), BucketList.class);

                String CalculatedCal = CalculateCal(getCaloriesFromCSV(str));

                if (dbHelper.doesItemExist(email, type + " " + str + " " + wt)) {
                    dbHelper.insertItemData(email, type + " " + str + " " + wt + "(" + Integer.toString(i++) + ")", CalculatedCal);
                    intent.putExtra("Email", email);
                    startActivity(intent);
                } else {
                    dbHelper.insertItemData(email, type + " " + str + " " + wt, CalculatedCal);
                    intent.putExtra("Email", email);
                    startActivity(intent);
                }
                searchView.setText("");
                fd_wd.setText("");
                type = "";
                alertDialog.dismiss();
            }
        });

        option3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "D";
                Intent intent = new Intent(getApplicationContext(), BucketList.class);

                String CalculatedCal = CalculateCal(getCaloriesFromCSV(str));

                if (dbHelper.doesItemExist(email, type + " " + str + " " + wt)) {
                    dbHelper.insertItemData(email, type + " " + str + " " + wt + "(" + Integer.toString(i++) + ")", CalculatedCal);
                    intent.putExtra("Email", email);
                    startActivity(intent);
                } else {
                    dbHelper.insertItemData(email, type + " " + str + " " + wt, CalculatedCal);
                    intent.putExtra("Email", email);
                    startActivity(intent);
                }
                searchView.setText("");
                fd_wd.setText("");
                type = "";
                alertDialog.dismiss();
            }
        });

        option4Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "S";
                Intent intent = new Intent(getApplicationContext(), BucketList.class);

                String CalculatedCal = CalculateCal(getCaloriesFromCSV(str));

                if (dbHelper.doesItemExist(email, type + " " + str + " " + wt)) {
                    dbHelper.insertItemData(email, type + " " + str + " " + wt + "(" + Integer.toString(i++) + ")", CalculatedCal);
                    intent.putExtra("Email", email);
                    startActivity(intent);
                } else {
                    dbHelper.insertItemData(email, type + " " + str + " " + wt, CalculatedCal);
                    intent.putExtra("Email", email);
                    startActivity(intent);
                }
                searchView.setText("");
                fd_wd.setText("");
                type = "";
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void startTextChangeLoop() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Change text with animation
                changeText();
                // Repeat after 2 seconds
                handler.postDelayed(this, 2000);
            }
        }, 2000); // Start after 2 seconds
    }

    private void changeText() {
        // Increment the index
        currentTextIndex = (currentTextIndex + 1) % textArray.length;
        searchView.setHint(textArray[currentTextIndex]);
    }

    private boolean areNotificationPermissionsGranted() {
        return NotificationManagerCompat.from(this).areNotificationsEnabled();
    }

    private void requestNotificationPermissions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.custom_alert_signout, null);

        TextView titleTextView = dialogView.findViewById(R.id.dialogTitle);
        titleTextView.setText("Enhance your app experience – grant notification permissions for efficiency.");
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

        TextView option1Button = dialogView.findViewById(R.id.option1Button);
        option1Button.setText("Continue");
        TextView option2Button = dialogView.findViewById(R.id.option2Button);
        option2Button.setText("OK");

        final AlertDialog alertDialog = builder.setView(dialogView).create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Window window = alertDialog.getWindow();
                if (window != null) {
                    window.setBackgroundDrawableResource(R.drawable.custom_alert_bg);
                }
            }
        });
        option2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAppNotificationSettings();
                alertDialog.dismiss();
            }
        });
        option1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void openAppNotificationSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        } else {
            intent.putExtra("app_package", getPackageName());
            intent.putExtra("app_uid", getApplicationInfo().uid);
        }

        startActivity(intent);
    }

    private void showSignoutConfirmationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.custom_alert_signout, null);

        TextView titleTextView = dialogView.findViewById(R.id.dialogTitle);
        titleTextView.setText("Are you sure?");

        TextView option1Button = dialogView.findViewById(R.id.option1Button);
        TextView option2Button = dialogView.findViewById(R.id.option2Button);

        final AlertDialog alertDialog = builder.setView(dialogView).create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Window window = alertDialog.getWindow();
                if (window != null) {
                    window.setBackgroundDrawableResource(R.drawable.custom_alert_bg);
                }
            }
        });
        option2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the app settings to allow the user to grant notification permissions
                // Clear the shared preferences and sign out from Firebase Authentication
                SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean("isSignedIn", false);
                editor.putString("currentUser", null);
                editor.apply();

                // Redirect to Login Activity
                Intent intent = new Intent(getApplicationContext(), Login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                alertDialog.dismiss();
            }
        });
        option1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void vibrateDevice(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null) {
            // Check if the device supports vibration effects
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                VibrationEffect effect = VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE);
                vibrator.vibrate(effect);
            } else {
                // For older devices without VibrationEffect
                vibrator.vibrate(200);
            }
        }
    }

    // Helper method to apply a shake animation to a view
    private void shakeView(View view) {
        Animation shake = AnimationUtils.loadAnimation(view.getContext(), R.anim.shake_animation);
        view.startAnimation(shake);
    }
}
