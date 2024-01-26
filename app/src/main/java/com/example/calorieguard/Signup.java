package com.example.calorieguard;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.calorieguard.R.id;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class Signup extends AppCompatActivity {

    public EditText name,email,pass,cnfpass;
    public TextView t,signupbtn,showpass,showcnfpass;
    private ProgressBar progressBar2;
    boolean isShown=false;
    boolean isShowncnf=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        Objects.requireNonNull(getSupportActionBar()).hide();

        signupbtn= findViewById(R.id.SignupBtn);
        name=(EditText)findViewById(R.id.editTextName);
        t=(TextView)findViewById(id.textView9);
        email=(EditText)findViewById(id.editText2email);
        pass=(EditText)findViewById(id.editText2password);
        progressBar2 = findViewById(id.progressBar2);
        progressBar2.setVisibility(View.GONE);
        showcnfpass=findViewById(id.showcnfpass);
        showpass=findViewById(id.showpass);
        cnfpass=findViewById(id.cnfpass);

        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        showpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isShown) {
                    showpass.setBackgroundResource(R.drawable.eye);
                    pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    isShown=!isShown;
                }
                else {
                    showpass.setBackgroundResource(R.drawable.show);
                    pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    isShown=!isShown;
                }
            }
        });

        showcnfpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isShowncnf) {
                    showcnfpass.setBackgroundResource(R.drawable.eye);
                    cnfpass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    isShowncnf=!isShowncnf;
                }
                else {
                    showcnfpass.setBackgroundResource(R.drawable.show);
                    cnfpass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    isShowncnf=!isShowncnf;
                }
            }
        });

        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email=email.getText().toString();
                String Pass=pass.getText().toString();
                String PassCnf=cnfpass.getText().toString();
                String Name=name.getText().toString();
                if(!Email.isEmpty() && !Pass.isEmpty() && !Name.isEmpty() && !PassCnf.isEmpty()){
                    if(Pass.trim().equals(PassCnf.trim()))
                    {
                        progressBar2.setVisibility(View.VISIBLE);
                        Fun_signup(Email,Pass.trim(),Name);
                        name.setText("");
                        email.setText("");
                        pass.setText("");
                        cnfpass.setText("");
                    }
                    else {
                        Toast.makeText(Signup.this, "Both the Password must be same!", Toast.LENGTH_SHORT).show();
                        shakeView(pass);
                        shakeView(cnfpass);
                        vibrateDevice(Signup.this);
                    }
               }
                else {
                    vibrateDevice(Signup.this);
                 if(Email.isEmpty())
                 {
                     shakeView(email);
                 }
                 if(Pass.isEmpty())
                 {
                     shakeView(pass);
                 }
                 if(PassCnf.isEmpty())
                 {
                     shakeView(cnfpass);
                 }
                 if(Name.isEmpty())
                 {
                     shakeView(name);
                 }
                }
            }
        });

    }

    public void Fun_signup(String Email, String Pass, String Name) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance("https://calorie-guard-412008-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");

        String sanitizedEmail = Email.replaceAll("[.#\\[\\]$@]", "_");
        final DatabaseReference userRef = mDatabase.child(sanitizedEmail).child("Data");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // The node already exists, show an error message
                    Toast.makeText(Signup.this, "Email already in use", Toast.LENGTH_SHORT).show();
                } else {
                    // The node doesn't exist, proceed with data insertion
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("name", Name);

                    mDatabase.child(sanitizedEmail).child("Data").setValue(hashMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    DBHelper dbHelper = new DBHelper(Signup.this);
                                    dbHelper.close();

                                    SecurePreferencesManager.saveCredentials(Signup.this, Email, Pass);

                                    Intent intent = new Intent(getApplicationContext(), UserData.class);
                                    intent.putExtra("Email", Email);
                                    intent.putExtra("Name", Name);
                                    intent.putExtra("DpUrl", "https://i.ibb.co/gvBsZ9q/Rectangle-2-1.png");
                                    startActivity(intent);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Signup.this, "Turn On your Internet: " + e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors during the database operation
                Toast.makeText(Signup.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
}
