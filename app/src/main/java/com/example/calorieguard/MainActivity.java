package com.example.calorieguard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Struct;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    public TextView Lweight, Gweight, CurrCal, User, result, HW,buttonsignout;
    public Button additem;
    public ImageView imageView2;
    public AutoCompleteTextView searchView;
    private ProgressBar progressBar;
    private DatabaseReference mDatabase;
    private static final String[] fooditems = new String[]{
            "Aloo Poshto",
            "Beguni",
            "Bhaja Muger Dal",
            "Bhapa Chingri",
            "Bholar Dal",
            "Bholar Kofta",
            "Bhetki Fish (1 piece)",
            "Brown Rice 100gm",
            "Chicken Soup 100gm",
            "Chingri Malai Curry",
            "Chitol Macher Muithya",
            "Cholar Kofta",
            "Cholar Dal",
            "Dhokar Dalna",
            "Doi Fuchka",
            "Fish Cutlet",
            "Ghugni",
            "Hajmola 3 tablets","Halwa","Hung Curd",
            "Ilish Bhapa",
            "Jhal Muri",
            "Kachuri",
            "Kalojam",
            "Katla Fish (1 piece)",
            "Khoi er Shingara",
            "Kochuri Torkari",
            "Kolar Bora",
            "Lau Ghonto",
            "Luchi Aloo Dom",
            "Maasur Dal 100gm",
            "Mishti Doi",
            "Murgir Jhol",
            "Narkel Chingri",
            "One Luchi",
            "Paneer 100gm",
            "Panta Bhaat",
            "Pati Shapta",
            "Payesh",
            "Phulkopir Dalna",
            "Roasted Chicken 100gm",
            "Roti 47gm",
            "Vaja alu","Wada","Xacuti","Yellow Dal","Zafrani Pulao"};

    String str;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#eb4034")));

        Lweight = (TextView) findViewById(R.id.Lweight);
        Gweight = (TextView) findViewById(R.id.Gweight);
        CurrCal = (TextView) findViewById(R.id.CurrCal);
        User = (TextView) findViewById(R.id.username);
        buttonsignout=(TextView)findViewById(R.id.buttonsignout);
        HW = (TextView) findViewById(R.id.HW);
        additem=(Button)findViewById(R.id.additem);
        result = (TextView) findViewById(R.id.result);
        imageView2=(ImageView)findViewById(R.id.imageView2);
        progressBar=(ProgressBar)findViewById(R.id.progressBar3);
        progressBar.setVisibility(View.VISIBLE);
        searchView = (AutoCompleteTextView)findViewById(R.id.searchView);

        HashMap<String, String> foodCalories = new HashMap<String, String>();
        foodCalories.put("Aloo Poshto", "257 cals");
        foodCalories.put("Beguni", "250 cals");
        foodCalories.put("Bhaja Muger Dal", "130 cals");
        foodCalories.put("Bhapa Chingri", "200 cals");
        foodCalories.put("Bholar Dal", "130 cals");
        foodCalories.put("Bholar Kofta", "190 cals");
        foodCalories.put("Bhetki Fish (1 piece)", "108 cals");
        foodCalories.put("Brown Rice 100gm", "116 cals");
        foodCalories.put("Chicken Soup 100gm", "63 cals");
        foodCalories.put("Chingri Malai Curry", "301 cals");
        foodCalories.put("Chitol Macher Muithya", "137 cals");
        foodCalories.put("Cholar Kofta", "178 cals");
        foodCalories.put("Cholar Dal", "174 cals");
        foodCalories.put("Dhokar Dalna", "260 cals");
        foodCalories.put("Doi Fuchka", "205 cals");
        foodCalories.put("Fish Cutlet", "187 cals");
        foodCalories.put("Ghugni", "231 cals");
        foodCalories.put("Ilish Bhapa", "264 cals");
        foodCalories.put("Jhal Muri", "508 cals");
        foodCalories.put("Kachuri", "207 cals");
        foodCalories.put("Kalojam", "220 cals");
        foodCalories.put("Katla Fish (1 piece)", "144 cals");
        foodCalories.put("Khoi er Shingara", "115 cals");
        foodCalories.put("Kochuri Torkari", "250 cals");
        foodCalories.put("Kolar Bora", "180 cals");
        foodCalories.put("Lau Ghonto", "166 cals");
        foodCalories.put("Luchi Aloo Dom", "500 cals");
        foodCalories.put("Maasur Dal 100gm", "120 cals");
        foodCalories.put("Mishti Doi", "150 cals");
        foodCalories.put("Murgir Jhol", "275 cals");
        foodCalories.put("Narkel Chingri", "290 cals");
        foodCalories.put("One Luchi", "125 cals");
        foodCalories.put("Paneer 100gm", "265 cals");
        foodCalories.put("Panta Bhaat", "103 cals");
        foodCalories.put("Pati Shapta", "170 cals");
        foodCalories.put("Payesh", "200 cals");
        foodCalories.put("Phulkopir Dalna", "227 cals");
        foodCalories.put("Roasted Chicken", "295 cals");
        foodCalories.put("Roti 47gm", "116 cals");
        foodCalories.put("Hajmola 3 tablets", "10 cals");
        foodCalories.put("Halwa", "387 cals");
        foodCalories.put("Hung Curd", "98 cals");
        foodCalories.put("Vaja alu", "300 cals");
        foodCalories.put("Wada", "100 cals");
        foodCalories.put("Xacuti", "321 cals");
        foodCalories.put("Yellow Dal", "106 cals");
        foodCalories.put("Zafrani Pulao", "207 cals");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, fooditems);
        searchView.setAdapter(adapter);

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Filterable adapter = (Filterable) searchView.getAdapter();
                adapter.getFilter().filter(s);
                searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            str= searchView.getText().toString();
                            searchView.clearFocus();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
                            result.setText(foodCalories.get(str));
                            return true;
                        }
                        return false;
                    }
                });
            }
        });

        String email = getIntent().getExtras().getString("Email");

            mDatabase = FirebaseDatabase.getInstance("https://calorie-guard-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");

            if (email.equals("Anonymous")) {
                progressBar.setVisibility(View.GONE);
                buttonsignout.setVisibility(View.GONE);
                additem.setVisibility(View.GONE);
                User.setText(email);
                CurrCal.setText("0");
                Lweight.setText("0");
                Gweight.setText("0");
                HW.setTextColor(Color.BLUE);
                HW.setText("   Have account?\n   Log In");
                imageView2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "Sign In to access BucketList", Toast.LENGTH_SHORT).show();
                    }
                });
                HW.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(getApplicationContext(),Login.class);
                        startActivity(intent);
                    }
                });
            }
            else {
                imageView2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent= new Intent(getApplicationContext(),BucketList.class);
                        intent.putExtra("Email",email);
                        startActivity(intent);
                    }
                });
                buttonsignout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferences preferences = getSharedPreferences("myPrefs", MODE_PRIVATE);
                        preferences.edit().clear().apply();

                        // Sign out from Firebase Authentication
                        FirebaseAuth.getInstance().signOut();

                        // Redirect to Login Activity
                        Intent intent = new Intent(getApplicationContext(), Login.class);
                        startActivity(intent);
                        finish();
                    }
                });
                Get_User_Data(email);

            }

            additem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    str= searchView.getText().toString();
                    Intent intent=new Intent(getApplicationContext(),BucketList.class);
                    if(!str.isEmpty() && foodCalories.get(str)!=null)
                    {
                        mDatabase.child(email).child("Items").child(str).setValue(foodCalories.get(str))
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MainActivity.this, ""+e.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                        intent.putExtra("Email",email);
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "Select a valid Item", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

        public void Get_User_Data (String email){
            mDatabase.child(email).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);
                        DataSnapshot dataSnapshot = task.getResult();
                        // Retrieve the email map
                        HashMap<String, String> emailMap = new HashMap<>();
                        emailMap = (HashMap<String, String>) dataSnapshot.getValue();

                        if (emailMap != null) {
                            // Retrieve the values from the email map
                            String age = emailMap.get("age");
                            String height = emailMap.get("height");
                            String name = emailMap.get("name");
                            String weight = emailMap.get("weight");
                            String sex = emailMap.get("sex");

                            assert sex != null;
                            if (sex.equals("Male")) {
                                int curr_cal_men = (int) Math.round(66.47 + (10 * Float.parseFloat(weight)) + (5.003 * Float.parseFloat(height)) - (6.755 * Float.parseFloat(age)));
                                CurrCal.setText(Integer.toString(curr_cal_men) + " cals");
                                Lweight.setText(Integer.toString(curr_cal_men - 500));
                                Gweight.setText(Integer.toString(curr_cal_men + 500));
                                User.setText("Mr. " + name);
                                HW.setText("Height: " + height + " cm\nWeight: " + weight + " Kg\nAge: "+age+" yr");
                            } else if (sex.equals("Female")) {
                                int curr_cal_women = (int) Math.round(655.1 + (9.563 * Float.parseFloat(weight)) + (1.850 * Float.parseFloat(height)) - (4.676 * Float.parseFloat(age)));
                                CurrCal.setText(Integer.toString(curr_cal_women) + " cals");
                                Lweight.setText(Integer.toString(curr_cal_women - 500));
                                Gweight.setText(Integer.toString(curr_cal_women + 500));
                                User.setText("Miss " + name);
                                HW.setText("Height: " + height + " cm\nWeight: " + weight + " Kg\nAge: "+age+" yr");
                            }

                            Log.d("firebase", "Name: " + name + ", Age: " + age + ", Height: " + height + ", Weight: " + weight);
                        } else {
                            Log.e("firebase", "Email map is null");
                            Toast.makeText(MainActivity.this, "Email map is null", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("firebase", "Error getting data", task.getException());
                        Toast.makeText(MainActivity.this, "Error getting data", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


}
