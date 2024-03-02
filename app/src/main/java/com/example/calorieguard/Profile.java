package com.example.calorieguard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Profile extends AppCompatActivity {
    EditText age,height,w1,w2,city;
    String Email;
    TextView edt_btn,save,Name,activity;
    Spinner sex,unit;
    ImageView dp,back;
    boolean flg=false;
    boolean edt_enable=false;
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Profile.this, MainActivity.class);
        intent.putExtra("Email", Email);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Objects.requireNonNull(getSupportActionBar()).hide();

        Email = getIntent().getExtras().getString("Email");

        DBHelper dbHelper = new DBHelper(this);
        dbHelper.close();

        Name=findViewById(R.id.edt_name);
        dp=findViewById(R.id.dp_edt);
        age=findViewById(R.id.edt_age);
        height=findViewById(R.id.edt_height);
        w1=findViewById(R.id.edt_w1);
        w2=findViewById(R.id.edt_w2);
        city=findViewById(R.id.edt_city);
        sex=findViewById(R.id.edt_sex);
        activity=findViewById(R.id.edt_activity);
        edt_btn=findViewById(R.id.edt_data_btn);
        save=findViewById(R.id.save_edt_btn);
        back=findViewById(R.id.back);
        unit=findViewById(R.id.unit_p);

        sex.setEnabled(false);
        activity.setEnabled(false);
        age.setEnabled(false);
        height.setEnabled(false);
        w1.setEnabled(false);
        w2.setEnabled(false);
        city.setEnabled(false);
        save.setVisibility(View.GONE);


        HashMap<String, String> userData = dbHelper.getUserDataById(Email);
        String name = userData.get("name");
        String weight = userData.get("weight");
        String Height = userData.get("height");
        double height_val =roundToTwoDecimalPlace(Double.parseDouble(Height));
        Height = String.valueOf(height_val);

        String Age = userData.get("age");
        String City = userData.get("city");
        String aimedWeight = userData.get("aimedWeight");
        String ActivityLevel= userData.get("activityLevel");
        String[] activityParts = ActivityLevel.split(":");
        String Sex = userData.get("sex");
        String plan= userData.get("plan");
        String dpUrl = userData.get("DpUrl");

        Name.setText(name);
        activity.setHint(ActivityLevel);
        w1.setText(weight);
        w2.setText(aimedWeight);
        city.setText(City);
        age.setText(Age);
        height.setText(Height);


        Glide.with(Profile.this)
                .load(dpUrl)
                .apply(new RequestOptions().placeholder(R.drawable.pp).error(R.drawable.pp).transform(new CircleCrop()))
                .into(dp);


        String[] genderOptions = {"Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                genderOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sex.setAdapter(adapter);


       activity.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               if(edt_enable) {
                   showActivitySpinner();
               }
           }
       });


        int position = adapter.getPosition(Sex);
        sex.setSelection(position);

        String[] units = {"cm", "feet"};
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                units
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unit.setAdapter(adapter2);

        unit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Check if the selected item is "Select your Activity Level"
                if(flg && !unit.getSelectedItem().toString().isEmpty())
                {
                    if(unit.getSelectedItem().toString().equals("feet"))
                    {
                        height.setText(cmToFeetInches(Double.parseDouble(height.getText().toString())));
                    }
                    else{
                        height.setText(feetInchesToCm(height.getText().toString()));
                    }
                }
                flg=true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });


        edt_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sex.setEnabled(true);
                activity.setEnabled(true);
                age.setEnabled(true);
                height.setEnabled(true);
                w1.setEnabled(true);
                w2.setEnabled(true);
                city.setEnabled(true);
                edt_enable=true;
                activity.setText(ActivityLevel);
                save.setVisibility(View.VISIBLE);

                int semigreyColor = getResources().getColor(R.color.black);
                age.setTextColor(semigreyColor);
                height.setTextColor(semigreyColor);
                w1.setTextColor(semigreyColor);
                w2.setTextColor(semigreyColor);
                city.setTextColor(semigreyColor);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sex.setEnabled(false);
                activity.setEnabled(false);
                age.setEnabled(false);
                height.setEnabled(false);
                w1.setEnabled(false);
                w2.setEnabled(false);
                city.setEnabled(false);
                save.setVisibility(View.GONE);

                int greyColor = getResources().getColor(R.color.lightgrey);
                age.setTextColor(greyColor);
                height.setTextColor(greyColor);
                w1.setTextColor(greyColor);
                w2.setTextColor(greyColor);
                activity.setTextColor(greyColor);
                city.setTextColor(greyColor);

                String name = Name.getText().toString().trim();
                String weight = w1.getText().toString().trim();
                String Height = height.getText().toString().trim();
              if(unit.getSelectedItem().toString().equals("feet"))
                {
                    Height=feetInchesToCm(Height);
                }
                String Age = age.getText().toString().trim();
                String City = city.getText().toString().trim();
                String aimedWeight = w2.getText().toString().trim();
                String ActivityLevel= activity.getText().toString().trim();
                String Sex = sex.getSelectedItem().toString();

                if (!Height.isEmpty() && !weight.isEmpty() && !Age.isEmpty() && !City.isEmpty() && !sex.equals("Select Gender") && !ActivityLevel.equals("Select your Activity Level") && !aimedWeight.isEmpty()) {
                    String activity_float="";
                    if (activityParts.length == 2) {
                        String activityPrefix = activityParts[0].trim();
                        switch (activityPrefix) {
                            case "Sedentary Activity":
                                activity_float = "1.2";
                                break;
                            case "Lightly Active":
                                activity_float = "1.375";
                                break;
                            case "Moderately Active":
                                activity_float = "1.55";
                                break;
                            case "Very Active":
                                activity_float = "1.725";
                                break;
                            case "Extra Active":
                                activity_float = "1.9";
                                break;
                        }
                    }

                    SaveData(plan,Email,Age,Height,weight,Sex,name,City,dpUrl,aimedWeight,activity_float,ActivityLevel);

                    Intent intent = new Intent(Profile.this, MainActivity.class);
                    intent.putExtra("Email", Email);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    startActivity(intent);
                    finish();
                } else {
                    vibrateDevice(Profile.this);
                    if(Age.isEmpty())
                    {
                        shakeView(age);
                    }
                    if(ActivityLevel.equals("Select your Activity Level"))
                    {
                        shakeView(activity);
                    }
                    if(Height.isEmpty())
                    {
                        shakeView(height);
                    }
                    if(Sex.equals("Select Gender"))
                    {
                        shakeView(sex);
                    }
                    if(weight.isEmpty())
                    {
                        shakeView(w1);
                    }
                    if(aimedWeight.isEmpty())
                    {
                        shakeView(w2);
                    }
                    if(City.isEmpty())
                    {
                        shakeView(city);
                    }
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Profile.this, MainActivity.class);
                intent.putExtra("Email", Email);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                startActivity(intent);
                finish();
            }
        });

    }

    public void SaveData(String plan, String mod_email, String Age, String Height, String Weight, String Sex, String name, String City, String DpUrl, String Aimedwight, String Activitylevel_float,String ActivityLevel) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

// Create a Map to hold user data
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("age", Age);
        userData.put("height", Height);
        userData.put("weight", Weight);
        userData.put("sex", Sex);
        userData.put("city", City);
        userData.put("dpUrl", DpUrl);
        userData.put("aimedWeight", Aimedwight);
        userData.put("activityLevel", ActivityLevel);

// Construct the document reference using a sanitized email as the document ID
        String sanitizedEmail = mod_email.replaceAll("[.#\\[\\]$@]", "_");

// Add a collection if needed (assuming a collection named "Users" doesn't exist yet)
        db.collection("Users")
                .document(sanitizedEmail)
                .set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "User data saved successfully!");
                        // Handle success (e.g., show a success message)
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Error saving user data: ", e);
                        // Handle failure (e.g., show an error message)
                    }
                });


        DBHelper dbHelper = new DBHelper(this);
        dbHelper.close();

        String curr_cal = "";
        if (Sex.equals("Male")) {
            double BMI_AL_Men = Float.parseFloat(Activitylevel_float)*(66.47 + (10 * Float.parseFloat(Weight)) + (5.003 * Float.parseFloat(Height)) - (6.755 * Float.parseFloat(Age)));
            curr_cal = Integer.toString((int) Math.round(0.1*BMI_AL_Men+BMI_AL_Men));

        } else if (Sex.equals("Female")) {
            double BMI_AL_Women=Float.parseFloat(Activitylevel_float)*(655.1 + (9.563 * Float.parseFloat(Weight)) + (1.850 * Float.parseFloat(Height)) - (4.676 * Float.parseFloat(Age)));
            curr_cal = Integer.toString((int) Math.round(0.1*BMI_AL_Women+BMI_AL_Women));

        }
        dbHelper.updateUserData(mod_email, name, Weight, Height, Age, Sex, City, Aimedwight, curr_cal, plan, DpUrl, ActivityLevel);

    }

    private void shakeView(View view) {
        Animation shake = AnimationUtils.loadAnimation(view.getContext(), R.anim.shake_animation);
        view.startAnimation(shake);
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

    public static String feetInchesToCm(String feetInchesStr) {
        // Split the input string into feet and inches
        String[] parts = feetInchesStr.split("\\.");

        // Extract feet and inches
        int feet = Integer.parseInt(parts[0]);
        int inches = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;

        // Convert feet and inches to centimeters
        double totalInches = feet * 12 + inches;
        double centimeters = roundToTwoDecimalPlace(totalInches * 2.54);

        // Convert the result to a string
        return String.valueOf(centimeters);
    }

    public static String cmToFeetInches(double centimeters) {
        // Convert centimeters to inches
        double totalInches = centimeters / 2.54;

        // Calculate feet and remaining inches
        int feet = (int) (totalInches / 12);
        int remainingInches = (int) (totalInches % 12);

        // Convert the result to a string
        return feet + "." + remainingInches;
    }


    private void showActivitySpinner() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.custom_activity, null);

        TextView AL1 = dialogView.findViewById(R.id.AL1);
        TextView AL2 = dialogView.findViewById(R.id.AL2);
        TextView AL3 = dialogView.findViewById(R.id.AL3);
        TextView AL4 = dialogView.findViewById(R.id.AL4);
        TextView AL5 = dialogView.findViewById(R.id.AL5);


        final AlertDialog alertDialog = builder.setView(dialogView).create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Window window = alertDialog.getWindow();
                if (window != null) {
                    window.setBackgroundDrawableResource(R.drawable.custom_alert_activity_level_bg);
                }
            }
        });

        AL1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.setText(AL1.getText().toString().trim());
                alertDialog.dismiss();
            }
        });
        AL2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.setText(AL2.getText().toString().trim());
                alertDialog.dismiss();
            }
        });
        AL3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.setText(AL3.getText().toString().trim());
                alertDialog.dismiss();
            }
        });
        AL4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.setText(AL4.getText().toString().trim());
                alertDialog.dismiss();
            }
        });
        AL5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.setText(AL5.getText().toString().trim());
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    public static double roundToTwoDecimalPlace(double value) {
        // Use DecimalFormat to round off to one decimal place
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        return Double.parseDouble(decimalFormat.format(value));
    }

}