package com.example.calorieguard;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.collection.LLRBNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BucketList extends AppCompatActivity {
    private ListView listview;
    private ArrayAdapter adapter;
    private RadioGroup RadioG;
    private RadioButton RBL,RBM,RBG;
    TextView result,textview3;
    private Button addmore,delall;
    private DatabaseReference mDatabase;
    private ProgressBar progressBar6;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bucketlist);
        mDatabase = FirebaseDatabase.getInstance("https://calorie-guard-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");
        Toast.makeText(this, "Long press items to delete them", Toast.LENGTH_SHORT).show();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#eb4034")));

        listview = (ListView) findViewById(R.id.recyclerview);
        RadioG = (RadioGroup) findViewById(R.id.radioGroup2);
        addmore = (Button) findViewById(R.id.addmore);
        result = (TextView) findViewById(R.id.textView4);
        textview3=(TextView)findViewById(R.id.textView3);
        delall=(Button)findViewById(R.id.buttondel);
        RBL=(RadioButton)findViewById(R.id.looseweight);
        RBG=(RadioButton)findViewById(R.id.gainweight);
        RBM=(RadioButton)findViewById(R.id.maintainweight);
        progressBar6=(ProgressBar)findViewById(R.id.progressBar6);
        progressBar6.setVisibility(View.VISIBLE);
        HashMap<String, String> itemMap = new HashMap<String, String>();

        String Email = getIntent().getExtras().getString("Email");
        mDatabase.child(Email).child("Items").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task){
                if (task.isSuccessful()) {
                    progressBar6.setVisibility(View.GONE);
                    if (task.getResult() == null) {
                        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                        intent.putExtra("Email",Email);
                        startActivity(intent);
                        finish();
                    }
                        DataSnapshot dataSnapshot = task.getResult();
                    // Retrieve the item map
                    HashMap<String, String> tempMap = (HashMap<String, String>) dataSnapshot.getValue();
                    for (Map.Entry<String, String> entry : tempMap.entrySet()) {
                        itemMap.put(entry.getKey(), entry.getValue());
                    }
                    if (tempMap != null) {
                        Log.d("firebase", "Good");
                        String fd, cal;
                        ArrayList<String> arrayList = new ArrayList<String>();
                        adapter = new ArrayAdapter(BucketList.this, android.R.layout.simple_list_item_1, arrayList);
                        listview.setAdapter(adapter);
                        int sum = 0;
                        for (Map.Entry<String, String> entry : itemMap.entrySet()) {
                            fd = entry.getKey();
                            cal = entry.getValue();
                            sum += Integer.parseInt(Tocal(cal));
                            arrayList.add(fd + " : " + cal);
                        }

                        result.setText("Total food added : " + sum + " cals");

                        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                final int which_item = position;

                                new AlertDialog.Builder(BucketList.this)
                                        .setIcon(R.drawable.ic_baseline_delete_forever_24)
                                        .setTitle("Are you sure?")
                                        .setMessage("Do you want to delete " + parent.getItemAtPosition(position) + "?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                String r = (String) parent.getItemAtPosition(position);
                                                mDatabase.child(Email).child("Items").child(RemovedataType(r)).removeValue()
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Toast.makeText(BucketList.this, "" + e.toString(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                arrayList.remove(which_item);
                                                adapter.notifyDataSetChanged();
                                                finish();
                                                overridePendingTransition(0, 0);
                                                startActivity(getIntent());
                                                overridePendingTransition(0, 0);
                                            }
                                        })
                                        .setNegativeButton("No", null)
                                        .show();
                                return true;
                            }
                        });
                    } else {
                        Log.e("firebase", "Email map is null");
                        Toast.makeText(BucketList.this, "Email map is null", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(BucketList.this, "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        delall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child(Email).child("Items").removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                                intent.putExtra("Email",Email);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(BucketList.this, "" + e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        mDatabase.child(Email).child("Plan").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    String Plan = (String) dataSnapshot.getValue();

                    mDatabase.child(Email).child("curcal").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.isSuccessful()) {
                                DataSnapshot dataSnapshot = task.getResult();
                                String Curcal = (String) dataSnapshot.getValue();
                                String Daily = result.getText().toString();

                                if (Plan.equals("looseweight")) {
                                    RBL.setTextColor(getResources().getColor(R.color.green));
                                    int c=Integer.parseInt(Curcal)-500;
                                    textview3.setText("Your Daily Limit : " + Integer.toString(c));

                                    if (Integer.parseInt(GetDaily(Daily)) >= (Integer.parseInt(Curcal) - 500)) {
                                        Toast.makeText(BucketList.this, "Daily Limit Crossed !", Toast.LENGTH_SHORT).show();
                                        Toast.makeText(BucketList.this, "Delete an item", Toast.LENGTH_LONG).show();
                                        result.setTextColor(Color.RED);
                                        RBL.setTextColor(Color.RED);
                                    } else {
                                        result.setTextColor(getResources().getColor(R.color.green));
                                    }
                                } else if (Plan.equals("maintainweight")) {
                                    RBM.setTextColor(getResources().getColor(R.color.green));
                                    textview3.setText("Your Daily Limit : " + Curcal);
                                    if (Integer.parseInt(GetDaily(Daily)) >= Integer.parseInt(Curcal)) {
                                        Toast.makeText(BucketList.this, "Daily Limit Crossed !", Toast.LENGTH_SHORT).show();
                                        Toast.makeText(BucketList.this, "Delete an item", Toast.LENGTH_LONG).show();
                                        result.setTextColor(Color.RED);
                                        RBM.setTextColor(Color.RED);
                                    } else {
                                        result.setTextColor(getResources().getColor(R.color.green));
                                    }
                                } else {
                                    RBG.setTextColor(getResources().getColor(R.color.green));
                                    int c=Integer.parseInt(Curcal)+500;
                                    textview3.setText("Your Daily Limit : " + Integer.toString(c));
                                    if (Integer.parseInt(GetDaily(Daily)) >= (Integer.parseInt(Curcal) + 500)) {
                                        Toast.makeText(BucketList.this, "Daily Limit Crossed !", Toast.LENGTH_SHORT).show();
                                        Toast.makeText(BucketList.this, "Delete an item", Toast.LENGTH_LONG).show();
                                        result.setTextColor(Color.RED);
                                        RBG.setTextColor(Color.RED);
                                    } else {
                                        result.setTextColor(getResources().getColor(R.color.green));
                                    }
                                }
                            } else {
                                Log.e("firebase", "Error getting data", task.getException());
                                Toast.makeText(BucketList.this, "Error getting data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    Log.e("firebase", "Error getting data", task.getException());
                    Toast.makeText(BucketList.this, "Error getting data", Toast.LENGTH_SHORT).show();
                }
            }});


        RadioG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (RadioG.getCheckedRadioButtonId() == R.id.looseweight) {
                    mDatabase.child(Email).child("Plan").setValue("looseweight")
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(BucketList.this, "" + e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);

                } else if (RadioG.getCheckedRadioButtonId() == R.id.maintainweight) {
                    mDatabase.child(Email).child("Plan").setValue("maintainweight")
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(BucketList.this, "" + e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);

                } else {
                    mDatabase.child(Email).child("Plan").setValue("gainweight")
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(BucketList.this, "" + e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);

                }
            }
        });


        addmore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra("Email", Email);
                startActivity(i);
                finish();
            }
        });

    }
    public String GetDaily(String s)
    {
        String d="";
        s=s.trim();
        for(int i=18;i<=s.length();i++)
        {
            if(s.charAt(i)=='c')
            {
                break;
            }
            else
            {
                d+=s.charAt(i);
            }
        }
        return d.trim();
    }

    public String RemovedataType(String s) {
        String f = "";
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ':') {
                break;
            } else {
                f += s.charAt(i);
            }
        }
        return f.trim();
    }
    public String Tocal(String s)
    {
        String cal="";
        for(int i=0;i<s.length();i++)
        {
            if(s.charAt(i)=='c')
            {
                break;
            }
            else
            {
                cal+=s.charAt(i);
            }
        }
        return cal.trim();
    }
}