package com.example.calorieguard;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;

import android.graphics.drawable.Drawable;

import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;

import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;

import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import androidx.work.Data;

import androidx.work.OneTimeWorkRequest;

import androidx.work.WorkManager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class BucketList extends AppCompatActivity {
    @Override
    protected void onUserLeaveHint() {
        footer.Stop();
        super.onUserLeaveHint();
    }
    @Override
    public void onBackPressed() {
        footer.Stop();
        super.onBackPressed();
    }

    private ListView listview;
    private CustomAdapter adapter;
    private RadioGroup RadioG;
    private Footer footer;
    private RadioButton RBL, RBM, RBG;
    boolean programmaticCheck = false;
    private TextView result, textview3, addmore, delall, AnaPercentage, all, breakfast_f, lunch_f, dinner_f, snaks_f, emptylist;

    private ProgressBar progressBar6;
    private ProgressBar progressBarAna;
    public int progressvalue = 0;
    boolean isDeleteMode = false;
    private Handler handler = new Handler();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bucketlist);

        Objects.requireNonNull(getSupportActionBar()).hide();

        listview = findViewById(R.id.recyclerview);
        RadioG = (RadioGroup) findViewById(R.id.radioGroup2);
        addmore = findViewById(R.id.addmore);
        result = (TextView) findViewById(R.id.textView4);
        textview3 = (TextView) findViewById(R.id.textView3);
        delall = findViewById(R.id.buttondel);
        RBL = (RadioButton) findViewById(R.id.looseweight);
        RBG = (RadioButton) findViewById(R.id.gainweight);
        RBM = (RadioButton) findViewById(R.id.maintainweight);
        AnaPercentage = findViewById(R.id.percentage);
        progressBar6 = (ProgressBar) findViewById(R.id.progressBar6);
        progressBarAna = findViewById(R.id.progressBarana);
        View rootView = findViewById(android.R.id.content);
        all = findViewById(R.id.all);
        breakfast_f = findViewById(R.id.breakfast_f);
        lunch_f = findViewById(R.id.lunch_f);
        dinner_f = findViewById(R.id.dinner_f);
        snaks_f = findViewById(R.id.snacks_f);
        emptylist = findViewById(R.id.emptylist);

        progressBar6.setVisibility(View.VISIBLE);
        all.setBackgroundResource(R.drawable.filterbg);


        String Email = getIntent().getExtras().getString("Email");
        footer = new Footer(this, rootView, Email);
        footer.setListBackground(R.drawable.circlebg);

        HashMap<String, String> itemMap = new HashMap<String, String>();

        DBHelper dbHelper = new DBHelper(this);
        dbHelper.close();

        if (dbHelper.doesUserExist(Email)) {
            itemMap = dbHelper.getAllItemsByUserId(Email);
            String fd, cal;
            ArrayList<String> arrayList = new ArrayList<String>();
            int sum = 0;
            for (Map.Entry<String, String> entry : itemMap.entrySet()) {
                fd = entry.getKey();
                cal = entry.getValue();
                sum += Integer.parseInt(Tocal(cal));
                arrayList.add(fd.substring(2) + " : " + cal);
            }

            if (arrayList.isEmpty()) {
                emptylist.setVisibility(View.VISIBLE);
            }

            adapter = new CustomAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, arrayList);
            listview.setAdapter(adapter);

            result.setText(sum + " cal");
        } else {
            result.setText("0 cal");
            delall.setVisibility(View.GONE);
            ArrayList<String> arrayList = new ArrayList<String>();
            emptylist.setVisibility(View.VISIBLE);
            adapter = new CustomAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, arrayList);
            listview.setAdapter(adapter);
            progressBar6.setVisibility(View.GONE);
        }

        progressBar6.setVisibility(View.GONE);

        delall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isDeleteMode) {
                    // Delete selected items
                    ArrayList<String> selectedItems = adapter.getSelectedItems();
                    if (!selectedItems.isEmpty()) {
                        showDeleteConfirmationDialog(selectedItems, Email, adapter);
                    } else {
                        adapter.setDeleteMode(false);
                    }
                    isDeleteMode = false;
                    delall.setBackgroundResource(R.drawable.deleteallbg);
                } else {
                    // Enter delete mode
                    isDeleteMode = true;
                    adapter.setDeleteMode(true);
                    delall.setBackgroundResource(R.drawable.deleteicon);
                }
            }
        });

        if (dbHelper.doesUserExist(Email)) {
            String Plan = dbHelper.getPlanByUserId(Email);
            String Curcal = dbHelper.getCurcalByUserId(Email);

            String Daily = result.getText().toString();
            int anaCal;

            if (Plan.equals("looseweight")) {
                selectRadioButton(RadioG, R.id.looseweight);
                RBL.setTextColor(getResources().getColor(R.color.black));
                RBL.setTypeface(null, Typeface.BOLD);
                int c = Integer.parseInt(Curcal) - 500;
                anaCal = Integer.parseInt(Curcal) - 500;
                textview3.setText(Integer.toString(c) + " cal");

                if (Integer.parseInt(GetDaily(Daily)) >= (Integer.parseInt(Curcal) - 500)) {
                    Toast.makeText(BucketList.this, "Daily Limit Crossed !", Toast.LENGTH_SHORT).show();
                    vibrateDevice(BucketList.this);
                    result.setTextColor(Color.RED);
                    RBL.setTextColor(Color.RED);
                } else {
                    result.setTextColor(getResources().getColor(R.color.green));
                }
            } else if (Plan.equals("maintainweight")) {
                selectRadioButton(RadioG, R.id.maintainweight);
                RBM.setTextColor(getResources().getColor(R.color.black));
                RBM.setTypeface(null, Typeface.BOLD);
                anaCal = Integer.parseInt(Curcal);
                textview3.setText(Curcal + " cal");
                if (Integer.parseInt(GetDaily(Daily)) >= Integer.parseInt(Curcal)) {
                    Toast.makeText(BucketList.this, "Daily Limit Crossed !", Toast.LENGTH_SHORT).show();
                    vibrateDevice(BucketList.this);
                    result.setTextColor(Color.RED);
                    RBM.setTextColor(Color.RED);
                } else {
                    result.setTextColor(getResources().getColor(R.color.green));
                }
            } else {
                selectRadioButton(RadioG, R.id.gainweight);
                RBG.setTextColor(getResources().getColor(R.color.black));
                RBG.setTypeface(null, Typeface.BOLD);
                int c = Integer.parseInt(Curcal) + 500;
                anaCal = Integer.parseInt(Curcal) + 500;
                textview3.setText(Integer.toString(c) + " cal");
                if (Integer.parseInt(GetDaily(Daily)) >= (Integer.parseInt(Curcal) + 500)) {
                    Toast.makeText(BucketList.this, "Daily Limit Crossed !", Toast.LENGTH_SHORT).show();
                   vibrateDevice(BucketList.this);
                    result.setTextColor(Color.RED);
                    RBG.setTextColor(Color.RED);
                } else {
                    result.setTextColor(getResources().getColor(R.color.green));
                }
            }

            progressvalue = (int) (100 * Float.parseFloat(GetDaily(Daily)) / anaCal);
            float percentageAna = (float) (100 * Float.parseFloat(GetDaily(Daily)) / anaCal);
            percentageAna = Math.min(percentageAna, 100.0f); // Ensure the percentage does not exceed 100
            DecimalFormat decimalFormat = new DecimalFormat("0.##"); // Format to two decimal places
            String formattedPercentage = decimalFormat.format(percentageAna);

            AnaPercentage.setText(formattedPercentage + "%");

            handler = new Handler(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    // Update the UI with the fixed progress value
                    progressBarAna.setProgress(progressvalue);
                    if (progressvalue >= 90) {
                        Drawable circularProgressAlertDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.circular_progress_alert);
                        if (circularProgressAlertDrawable != null) {
                            progressBarAna.setProgressDrawable(circularProgressAlertDrawable);
                        }
                    }
                    return true;
                }
            });
            handler.sendEmptyMessage(0);
        }

        RadioG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (!programmaticCheck) {
                    if (RadioG.getCheckedRadioButtonId() == R.id.looseweight) {
                        dbHelper.updatePlanByUserId(Email, "looseweight");
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);

                    } else if (RadioG.getCheckedRadioButtonId() == R.id.maintainweight) {

                        dbHelper.updatePlanByUserId(Email, "maintainweight");
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);

                    } else {

                        dbHelper.updatePlanByUserId(Email, "gainweight");
                        finish();
                        overridePendingTransition(0, 0);
                        startActivity(getIntent());
                        overridePendingTransition(0, 0);

                    }
                }
            }
        });


        addmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (result.getCurrentTextColor() == Color.RED) {
                    showAddMoreConfirmationDialog(Email);
                } else {
                    Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                    intent.putExtra("Email", Email);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                }
            }
        });

        all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                all.setBackgroundResource(R.drawable.filterbg);
                breakfast_f.setBackgroundResource(R.drawable.filterbgdefault);
                lunch_f.setBackgroundResource(R.drawable.filterbgdefault);
                dinner_f.setBackgroundResource(R.drawable.filterbgdefault);
                snaks_f.setBackgroundResource(R.drawable.filterbgdefault);

                HashMap<String, String> itemMap = new HashMap<String, String>();

                if (dbHelper.doesUserExist(Email)) {
                    emptylist.setVisibility(View.GONE);
                    itemMap = dbHelper.getAllItemsByUserId(Email);
                    String fd, cal;
                    ArrayList<String> arrayList = new ArrayList<String>();
                    for (Map.Entry<String, String> entry : itemMap.entrySet()) {
                        fd = entry.getKey();
                        cal = entry.getValue();
                        arrayList.add(fd.substring(2) + " : " + cal);
                    }

                    if (arrayList.isEmpty()) {
                        emptylist.setVisibility(View.VISIBLE);
                    }

                    adapter = new CustomAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, arrayList);
                    listview.setAdapter(adapter);

                } else {
                    result.setText("0 cal");
                    delall.setVisibility(View.GONE);
                    ArrayList<String> arrayList = new ArrayList<String>();
                    emptylist.setVisibility(View.VISIBLE);
                    adapter = new CustomAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, arrayList);
                    listview.setAdapter(adapter);
                    progressBar6.setVisibility(View.GONE);
                }

            }
        });
        breakfast_f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                all.setBackgroundResource(R.drawable.filterbgdefault);
                breakfast_f.setBackgroundResource(R.drawable.filterbg);
                lunch_f.setBackgroundResource(R.drawable.filterbgdefault);
                dinner_f.setBackgroundResource(R.drawable.filterbgdefault);
                snaks_f.setBackgroundResource(R.drawable.filterbgdefault);

                SetListData('B', Email);
            }
        });
        lunch_f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                all.setBackgroundResource(R.drawable.filterbgdefault);
                breakfast_f.setBackgroundResource(R.drawable.filterbgdefault);
                lunch_f.setBackgroundResource(R.drawable.filterbg);
                dinner_f.setBackgroundResource(R.drawable.filterbgdefault);
                snaks_f.setBackgroundResource(R.drawable.filterbgdefault);

                SetListData('L', Email);
            }
        });
        dinner_f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                all.setBackgroundResource(R.drawable.filterbgdefault);
                breakfast_f.setBackgroundResource(R.drawable.filterbgdefault);
                lunch_f.setBackgroundResource(R.drawable.filterbgdefault);
                dinner_f.setBackgroundResource(R.drawable.filterbg);
                snaks_f.setBackgroundResource(R.drawable.filterbgdefault);

                SetListData('D', Email);
            }
        });
        snaks_f.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                all.setBackgroundResource(R.drawable.filterbgdefault);
                breakfast_f.setBackgroundResource(R.drawable.filterbgdefault);
                lunch_f.setBackgroundResource(R.drawable.filterbgdefault);
                dinner_f.setBackgroundResource(R.drawable.filterbgdefault);
                snaks_f.setBackgroundResource(R.drawable.filterbg);

                SetListData('S', Email);
            }
        });

    }

    public String GetDaily(String s) {
        String d = "";
        s = s.trim();
        for (int i = 0; i <= s.length(); i++) {
            if (s.charAt(i) == 'c') {
                break;
            } else {
                d += s.charAt(i);
            }
        }
        return d.trim();
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

    private void selectRadioButton(RadioGroup radioGroup, int radioButtonId) {
        RadioButton selectedRadioButton = findViewById(radioButtonId);
        programmaticCheck = true;
        selectedRadioButton.setChecked(true);
        programmaticCheck = false;
    }

    private void showDeleteConfirmationDialog(ArrayList<String> selectedItems, String Email, CustomAdapter adapter) {
        DBHelper dbHelper = new DBHelper(this);
        dbHelper.close();


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.custom_alert_dellist, null);

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
// Delete selected items from the database
                for (String selectedItem : selectedItems) {
                    dbHelper.deleteItemByUserIdAndItemId(Email, "S " + extractAndTrim(selectedItem));
                    dbHelper.deleteItemByUserIdAndItemId(Email, "B " + extractAndTrim(selectedItem));
                    dbHelper.deleteItemByUserIdAndItemId(Email, "D " + extractAndTrim(selectedItem));
                    dbHelper.deleteItemByUserIdAndItemId(Email, "L " + extractAndTrim(selectedItem));
                }
                Intent intent = getIntent();
                finish();
                startActivity(intent, ActivityOptions.makeCustomAnimation(BucketList.this, R.anim.fade_in, R.anim.fade_out).toBundle());

                alertDialog.dismiss();
            }
        });
        option1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.setDeleteMode(false);
                isDeleteMode=false;
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    public void SetListData(char t, String Email) {
        DBHelper dbHelper = new DBHelper(this);
        dbHelper.close();

        HashMap<String, String> itemMap = new HashMap<String, String>();

        if (dbHelper.doesUserExist(Email)) {
            emptylist.setVisibility(View.GONE);
            itemMap = dbHelper.getAllItemsByUserId(Email);
            String fd, cal;
            ArrayList<String> arrayList = new ArrayList<String>();
            for (Map.Entry<String, String> entry : itemMap.entrySet()) {
                fd = entry.getKey();
                cal = entry.getValue();
                if (fd.charAt(0) == t) {
                    arrayList.add(fd.substring(2) + " : " + cal);
                }
            }

            if (arrayList.isEmpty()) {
                emptylist.setVisibility(View.VISIBLE);
            }

            adapter = new CustomAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, arrayList);
            listview.setAdapter(adapter);

        } else {
            result.setText("0 cal");
            delall.setVisibility(View.GONE);
            ArrayList<String> arrayList = new ArrayList<String>();
            emptylist.setVisibility(View.VISIBLE);
            adapter = new CustomAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, arrayList);
            listview.setAdapter(adapter);
            progressBar6.setVisibility(View.GONE);
        }
    }

    public String extractAndTrim(String input) {
        // Find the index of ":"
        int colonIndex = input.indexOf(":");

        return input.substring(0, colonIndex).trim();
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

    private void showAddMoreConfirmationDialog(String Email) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.custom_alert_signout, null);

        TextView titleTextView = dialogView.findViewById(R.id.dialogTitle);
        titleTextView.setText("You want to Add more?");

        TextView option1Button = dialogView.findViewById(R.id.option1Button);
        TextView option2Button = dialogView.findViewById(R.id.option2Button);
        option2Button.setText("add more");

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
                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                intent.putExtra("Email", Email);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
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

}