package com.example.calorieguard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserData extends AppCompatActivity {
    public Button next;
    public TextView activity_spinner,link;
    private LinearLayout l1;
    public EditText age, height, weight, city, nm,aimed_wt;
    public Spinner genderSpinner,unit;
    private ProgressBar progressBar2;
    private CheckBox terms;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_data);
        Objects.requireNonNull(getSupportActionBar()).hide();

        next = (Button) findViewById(R.id.button);
        age = (EditText) findViewById(R.id.age);
        height = (EditText) findViewById(R.id.editTextheight1);
        weight = (EditText) findViewById(R.id.editTextweight1);
        genderSpinner = findViewById(R.id.gender_spinner);
        activity_spinner=findViewById(R.id.activity_spinner);
        unit=findViewById(R.id.unit);
        city = findViewById(R.id.city);
        progressBar2 = findViewById(R.id.progressBar3);
        nm = (EditText) findViewById(R.id.editTextText);
        aimed_wt=findViewById(R.id.aimed_wt);
        terms=findViewById(R.id.terms);
        link=findViewById(R.id.link);
        l1=findViewById(R.id.l1);

        progressBar2.setVisibility(View.GONE);

        String[] genderOptions = {"Select Gender","Male", "Female"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                genderOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Check if the selected item is "Select your Activity Level"
                if (position == 0) {
                    ((TextView) parentView.getChildAt(0)).setTextColor(Color.GRAY); // Set color to grey
                } else {
                    ((TextView) parentView.getChildAt(0)).setTextColor(Color.BLACK); // Set color to black
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });

       activity_spinner.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               showActivitySpinner();
           }
       });

        String[] units = {"cm","feet"};
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                units
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unit.setAdapter(adapter2);

        String mod_email = getIntent().getExtras().getString("Email");
        checkAndRequestLocationPermission(mod_email,UserData.this);
        String name = getIntent().getExtras().getString("Name");
        String dpUrl = getIntent().getExtras().getString("DpUrl");
        nm.setText(name);

        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String websiteUrl = "https://sites.google.com/view/calorie-guard21/home";

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl));
                startActivity(intent);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Height = height.getText().toString();
                if(unit.getSelectedItem().toString().equals("feet"))
                {
                    Height=feetInchesToCm(Height);
                }
                String Weight = weight.getText().toString().trim();
                String Age = age.getText().toString().trim();
                String City = city.getText().toString().trim();
                String sex = genderSpinner.getSelectedItem().toString().trim();
                String activitylevel = activity_spinner.getText().toString().trim();
                String[] activityParts = activitylevel.split(":");
                String aimedweight = aimed_wt.getText().toString().trim();
                String name_edt = nm.getText().toString().trim();
                if (terms.isChecked() && !Height.isEmpty() && !Weight.isEmpty() && !Age.isEmpty() && !City.isEmpty() && !sex.equals("Select Gender") && !activitylevel.isEmpty() && !aimedweight.isEmpty() && !name_edt.isEmpty()) {
                    progressBar2.setVisibility(View.VISIBLE);
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

                    SaveData(mod_email, Age, Height, Weight, sex, name_edt, City, dpUrl, aimedweight, activity_float,activitylevel);
                    progressBar2.setVisibility(View.GONE);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtra("Email", mod_email);
                    startActivity(intent);

                    sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isSignedIn", true);
                    editor.putString("currentUser",mod_email);
                    editor.apply();

                    age.setText("");
                    height.setText("");
                    weight.setText("");
                    nm.setText("");
                    city.setText("");
                    aimed_wt.setText("");
                    activity_spinner.setText("");

                    int position = adapter.getPosition("Select Gender");
                    genderSpinner.setSelection(position);
                    finish();
                } else {
                    vibrateDevice(UserData.this);
                    if(!terms.isChecked())
                    {
                        shakeView(l1);
                    }
                    if(name_edt.isEmpty())
                    {
                        shakeView(nm);
                    }
                    if(Age.isEmpty())
                    {
                        shakeView(age);
                    }
                    if(activitylevel.equals("Select your Activity Level"))
                    {
                        shakeView(activity_spinner);
                    }
                    if(Height.isEmpty())
                    {
                        shakeView(height);
                    }
                    if(sex.equals("Select Gender"))
                    {
                        shakeView(genderSpinner);
                    }
                    if(Weight.isEmpty())
                    {
                        shakeView(weight);
                    }
                    if(aimedweight.isEmpty())
                    {
                        shakeView(aimed_wt);
                    }
                    if(City.isEmpty())
                    {
                        shakeView(city);
                    }
                }
            }
        });
    }

    public void SaveData(String mod_email, String Age, String Height, String Weight, String Sex, String name, String City, String DpUrl, String Aimedwight, String Activitylevel_float,String ActivityLevel) {

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

//        DatabaseReference mDatabase = FirebaseDatabase.getInstance("https://calorie-guard-412008-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
//
//        HashMap<String, Object> hashMap=new HashMap<>();
//        hashMap.put("name",name);
//        hashMap.put("age",Age);
//        hashMap.put("height",Height);
//        hashMap.put("weight",Weight);
//        hashMap.put("sex",Sex);
//        hashMap.put("city",City);
//        hashMap.put("dpUrl",DpUrl);
//        hashMap.put("aimedWeight",Aimedwight);
//        hashMap.put("activityLevel",ActivityLevel);
//
//        mDatabase.child(mod_email.replaceAll("[.#\\[\\]$@]", "_")).child("Data").setValue(hashMap)
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Toast.makeText(UserData.this, "Turn On your Internet : "+e.toString(), Toast.LENGTH_SHORT).show();
//                    }
//                });


        DBHelper dbHelper = new DBHelper(this);
        dbHelper.close();

        String curr_cal = "";
        if (Sex.equals("Male")) {
            double BMI_AL_Men = Float.parseFloat(Activitylevel_float)*(66.47 + (10 * Float.parseFloat(Weight)) + (5.003 * Float.parseFloat(Height)) - (6.755 * Float.parseFloat(Age)));
            curr_cal = Integer.toString((int) Math.round(0.1*BMI_AL_Men+BMI_AL_Men));
//            hashMap.put("curcal", Integer.toString(curr_cal_men));
        } else if (Sex.equals("Female")) {
            double BMI_AL_Women=Float.parseFloat(Activitylevel_float)*(655.1 + (9.563 * Float.parseFloat(Weight)) + (1.850 * Float.parseFloat(Height)) - (4.676 * Float.parseFloat(Age)));
            curr_cal = Integer.toString((int) Math.round(0.1*BMI_AL_Women+BMI_AL_Women));
//            hashMap.put("curcal", Integer.toString(curr_cal_women));
        }
        dbHelper.insertUserData(mod_email, name, DpUrl, Weight, Height, Age, City, Aimedwight, curr_cal, "maintainweight", Sex,ActivityLevel);

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isSignedIn", true);
        editor.putString("currentUser", mod_email);
        editor.apply();
    }

    public String ID="";
    public Context CONTEXT;
    private void checkAndRequestLocationPermission(String id,Context context) {
        CONTEXT=context;
        ID=id;
        LocationUpdater locationUpdater = new LocationUpdater(CONTEXT);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, proceed with location updates
            locationUpdater.requestLocationUpdates(id.replaceAll("[.#\\[\\]$@]", "_"),city);
        } else {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LocationUpdater locationUpdater = new LocationUpdater(CONTEXT);
                // Permission granted, proceed with location updates
                locationUpdater.requestLocationUpdates(ID.replaceAll("[.#\\[\\]$@]", "_"),city);
            } else {
                // Permission denied, handle accordingly (show a message, disable location-related functionality, etc.)
                Log.d("Permissoin Error","Denied");
            }
        }
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
        double centimeters = totalInches * 2.54;

        // Convert the result to a string
        return String.valueOf(centimeters);
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
                activity_spinner.setText(AL1.getText().toString().trim());
                alertDialog.dismiss();
            }
        });
        AL2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity_spinner.setText(AL2.getText().toString().trim());
                alertDialog.dismiss();
            }
        });
        AL3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity_spinner.setText(AL3.getText().toString().trim());
                alertDialog.dismiss();
            }
        });
        AL4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity_spinner.setText(AL4.getText().toString().trim());
                alertDialog.dismiss();
            }
        });
        AL5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity_spinner.setText(AL5.getText().toString().trim());
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }
}