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
import com.google.android.gms.tasks.OnSuccessListener;
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
            "Boiled White Rice 100gm",
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
            "Aloo Gobi",
            "Baingan Bharta",
            "Biryani",
            "Butter Chicken",
            "Chana Masala",
            "1 Boiled egg",
            "Chapati",
            "Chicken Tikka Masala",
            "Chole Bhature",
            "Dahi Bhalla",
            "Dal Makhani",
            "Dosa",
            "Gulab Jamun",
            "Idli",
            "Jalebi",
            "Kadai Paneer",
            "Kheer",
            "Lassi",
            "Malai Kofta",
            "Mango Lassi",
            "Masala Dosa",
            "Matar Paneer",
            "Naan",
            "Pakora",
            "Palak Paneer",
            "Pani Puri",
            "Paratha",
            "Raita",
            "Rajma",
            "Rashagulla",
            "Singara",
            "Aloo Fry 100gm",
            "Tandoori Chicken",
            "Vada Pav",
            "Paneer 100gm",
            "Panta Bhaat",
            "Pati Shapta",
            "Payesh",
            "Phulkopir Dalna",
            "Roasted Chicken 100gm",
            "Roti 47gm",
            "Vaja alu",
            "Wada",
            "Xacuti",
            "Yellow Dal",
            "Zafrani Pulao",
            "Aloo Paratha",
            "Bhindi Masala",
            "Butter Naan",
            "Chicken Biryani",
            "Chilli Chicken",
            "Dal Fry",
            "Fish Curry",
            "Gobi Manchurian",
            "Keema Naan",
            "Brown Bread 1 slice",
            "Kulfi",
            "Mango Pickle",
            "Murg Malai Tikka",
            "Paneer Butter Masala",
            "Pav Bhaji",
            "Prawn Masala",
            "Rogan Josh",
            "Samosa Chaat",
            "Shahi Paneer",
            "Soya Chaap Masala",
            "Tandoori Roti",
            "Soyabean curry",
            "Veg Manchurian",
            "Veg Pulao"};

    String str;
    int i=2;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#eb4034")));

       Toast t_buk=Toast.makeText(MainActivity.this, "Sign In to access BucketList", Toast.LENGTH_SHORT);
       Toast t_valid= Toast.makeText(MainActivity.this, "Select a valid Item", Toast.LENGTH_SHORT);

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
        foodCalories.put("Aloo fry 100gm", "274 cals");
        foodCalories.put("Brown Rice 100gm", "116 cals");
        foodCalories.put("Chicken Soup 100gm", "63 cals");
        foodCalories.put("Chingri Malai Curry", "301 cals");
        foodCalories.put("Chitol Macher Muithya", "137 cals");
        foodCalories.put("Cholar Kofta", "178 cals");
        foodCalories.put("Cholar Dal", "174 cals");
        foodCalories.put("Dhokar Dalna", "260 cals");
        foodCalories.put("Doi Fuchka", "205 cals");
        foodCalories.put("Aloo Gobi", "120 cals");
        foodCalories.put("Baingan Bharta", "150 cals");
        foodCalories.put("Biryani", "400 cals");
        foodCalories.put("Butter Chicken", "350 cals");
        foodCalories.put("Chana Masala", "200 cals");
        foodCalories.put("Chapati", "70 cals");
        foodCalories.put("Chicken Tikka Masala", "300 cals");
        foodCalories.put("Chole Bhature", "450 cals");
        foodCalories.put("Dahi Bhalla", "150 cals");
        foodCalories.put("Dal Makhani", "250 cals");
        foodCalories.put("1 Boiled egg", "155 cals");
        foodCalories.put("Dosa", "150 cals");
        foodCalories.put("Gulab Jamun", "150 cals");
        foodCalories.put("Idli", "40 cals");
        foodCalories.put("Jalebi", "150 cals");
        foodCalories.put("Kadai Paneer", "300 cals");
        foodCalories.put("Kheer", "250 cals");
        foodCalories.put("Lassi", "100 cals");
        foodCalories.put("Malai Kofta", "400 cals");
        foodCalories.put("Mango Lassi", "200 cals");
        foodCalories.put("Masala Dosa", "300 cals");
        foodCalories.put("Matar Paneer", "300 cals");
        foodCalories.put("Naan", "150 cals");
        foodCalories.put("Pakora", "150 cals");
        foodCalories.put("Palak Paneer", "250 cals");
        foodCalories.put("Pani Puri", "100 cals");
        foodCalories.put("Paratha", "150 cals");
        foodCalories.put("Aloo Paratha", "300 cals");
        foodCalories.put("Bhindi Masala", "200 cals");
        foodCalories.put("Butter Naan", "200 cals");
        foodCalories.put("Chicken Biryani", "450 cals");
        foodCalories.put("Chilli Chicken", "350 cals");
        foodCalories.put("Dal Fry", "200 cals");
        foodCalories.put("Fish Curry", "300 cals");
        foodCalories.put("Gobi Manchurian", "250 cals");
        foodCalories.put("Keema Naan", "350 cals");
        foodCalories.put("Kulfi", "150 cals");
        foodCalories.put("Soyabean curry", "323 cals");
        foodCalories.put("Mango Pickle", "50 cals");
        foodCalories.put("Murg Malai Tikka", "200 cals");
        foodCalories.put("Paneer Butter Masala", "300 cals");
        foodCalories.put("Pav Bhaji", "300 cals");
        foodCalories.put("Prawn Masala", "350 cals");
        foodCalories.put("Rogan Josh", "350 cals");
        foodCalories.put("Samosa Chaat", "250 cals");
        foodCalories.put("Shahi Paneer", "300 cals");
        foodCalories.put("Soya Chaap Masala", "300 cals");
        foodCalories.put("Tandoori Roti", "150 cals");
        foodCalories.put("Veg Manchurian", "200 cals");
        foodCalories.put("Veg Pulao", "350 cals");
        foodCalories.put("Raita", "100 cals");
        foodCalories.put("Rajma", "250 cals");
        foodCalories.put("Rashagulla", "150 cals");
        foodCalories.put("Singara", "250 cals");
        foodCalories.put("Tandoori Chicken", "250 cals");
        foodCalories.put("Vada Pav", "300 cals");
        foodCalories.put("Fish Cutlet", "187 cals");
        foodCalories.put("Ghugni", "231 cals");
        foodCalories.put("Ilish Bhapa", "264 cals");
        foodCalories.put("Jhal Muri", "508 cals");
        foodCalories.put("Kachuri", "207 cals");
        foodCalories.put("Kalojam", "220 cals");
        foodCalories.put("Katla Fish (1 piece)", "144 cals");
        foodCalories.put("Brown Bread 1 slice", "75 cals");
        foodCalories.put("Khoi er Shingara", "115 cals");
        foodCalories.put("Kochuri Torkari", "250 cals");
        foodCalories.put("Kolar Bora", "180 cals");
        foodCalories.put("Boiled White Rice 100gm", "200 cals");
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

                // Set an editor action listener for the search view to handle the "Done" action
                searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            // Get the search query and hide the keyboard
                            str= searchView.getText().toString();
                            searchView.clearFocus();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

                            // Display the result for the selected food item
                            result.setText(foodCalories.get(str));
                            return true;
                        }
                        return false;
                    }
                });
            }
        });

// Get the email passed from the previous activity and set up the Firebase database reference
        String email = getIntent().getExtras().getString("Email");
        mDatabase = FirebaseDatabase.getInstance("https://calorie-guard-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");

        if (email.equals("Anonymous")) {
            // If the user is anonymous, hide certain UI elements and show a message to prompt the user to log in or sign up
            progressBar.setVisibility(View.GONE);
            buttonsignout.setVisibility(View.GONE);
            additem.setVisibility(View.GONE);
            User.setText(email);
            CurrCal.setText("0");
            Lweight.setText("0");
            Gweight.setText("0");
            HW.setTextColor(Color.BLUE);
            HW.setText("   Have account?\n   Log In");

            // Set a click listener for the info icon to display a tooltip message
            imageView2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    t_buk.cancel();
                    t_buk.show();
                }
            });

            // Set a click listener for the "Log In" message to launch the Login activity
            HW.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(getApplicationContext(),Login.class);
                    startActivity(intent);
                }
            });
        } else {
            // If the user is not anonymous, show the list to the user
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
                    // Clear the shared preferences and sign out from Firebase Authentication
                    SharedPreferences preferences = getSharedPreferences("myPrefs", MODE_PRIVATE);
                    preferences.edit().clear().apply();
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
                        mDatabase.child(email).child("Items").child(str).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                            @Override
                            public void onSuccess(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    mDatabase.child(email).child("Items").child(str+"-"+Integer.toString(i++)).setValue(foodCalories.get(str))
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(MainActivity.this, "" + e.toString(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                    intent.putExtra("Email", email);
                                    startActivity(intent);
                                }
                                else
                                {
                                    mDatabase.child(email).child("Items").child(str).setValue(foodCalories.get(str))
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(MainActivity.this, "" + e.toString(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                    intent.putExtra("Email", email);
                                    startActivity(intent);
                                }
                            }});
                    }
                    else
                    {
                        t_valid.cancel();
                       t_valid.show();
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
