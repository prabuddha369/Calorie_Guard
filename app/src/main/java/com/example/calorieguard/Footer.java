package com.example.calorieguard;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class Footer{
    private Context context;
    private String email;
    private ImageView dashboard, lens, aichat, resturant, list;
    private long startTimeMillis;

    public Footer(Context context, View rootView, String email) {
        this.context = context;
        this.email = email;
        dashboard = rootView.findViewById(R.id.dashboard);
        lens = rootView.findViewById(R.id.lens);
        aichat = rootView.findViewById(R.id.AIchat);
//        resturant = rootView.findViewById(R.id.resturant);
        list = rootView.findViewById(R.id.list);
        startTimeMillis = SystemClock.elapsedRealtime();
        setClickListeners();
    }

    public void setDashboardBackground(int drawableResId) {
        dashboard.setBackgroundResource(drawableResId);
    }

    public void setListBackground(int drawableResId) {
        list.setBackgroundResource(drawableResId);
    }

    public void setLensBackground(int drawableResId) {
        lens.setBackgroundResource(drawableResId);
    }

    public void setChatBackground(int drawableResId) {
        aichat.setBackgroundResource(drawableResId);
    }

//    public void setResturantBackground(int drawableResId) {
//        resturant.setBackgroundResource(drawableResId);
//    }

    private void setClickListeners() {
        dashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleButtonClick(MainActivity.class);
            }
        });

        lens.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleButtonClick(Lens.class);
            }
        });

//        resturant.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                handleButtonClick(Resturants.class);
//            }
//        });

        aichat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleButtonClick(CHAT_BOT.class);
            }
        });

        list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleButtonClick(BucketList.class);
            }
        });
    }

    private void handleButtonClick(Class<? extends Activity> activityClass) {
        if (context instanceof Activity && !((Activity) context).getClass().getSimpleName().equals(activityClass.getSimpleName())) {

            Intent intent = new Intent(context, activityClass);
            intent.putExtra("Email", email);
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) context);
            context.startActivity(intent, options.toBundle());
            ((Activity) context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    private void stopTrackingAndPrint(Activity currentActivity) {
        long endTimeMillis = SystemClock.elapsedRealtime();
        long elapsedTimeMillis = endTimeMillis - startTimeMillis;
        double elapsedTimeSeconds = millisecondsToMinutes((double) elapsedTimeMillis);

        DatabaseReference mDatabase = FirebaseDatabase.getInstance("https://calorie-guard-412008-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");

        String userNode = email.replaceAll("[.#\\[\\]$@]", "_");
        String activityNode = currentActivity.getClass().getSimpleName();

        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String localTimestamp = inputDateFormat.format(new Date());

        try {
            Date date = inputDateFormat.parse(localTimestamp);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

            SimpleDateFormat outputDateFormat;

            if (hourOfDay >= 6 && hourOfDay < 12) {
                outputDateFormat = new SimpleDateFormat("yyyy-MM-dd : 6-12", Locale.getDefault());
            } else if (hourOfDay >= 12 && hourOfDay < 18) {
                outputDateFormat = new SimpleDateFormat("yyyy-MM-dd : 12-18", Locale.getDefault());
            } else if (hourOfDay >= 18) {
                outputDateFormat = new SimpleDateFormat("yyyy-MM-dd : 18-0", Locale.getDefault());
            } else {
                outputDateFormat = new SimpleDateFormat("yyyy-MM-dd : 0-6", Locale.getDefault());
            }

            localTimestamp = outputDateFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace(); // Handle parsing exception if necessary
        }


        DatabaseReference activityRef = mDatabase.child(userNode).child("Analysis").child(localTimestamp).child(activityNode);

        // Get the existing value
        activityRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Update the existing value with the new data
                    double existingValue = Double.parseDouble(Objects.requireNonNull(dataSnapshot.getValue(String.class)).replace(" sec", ""));
                    double updatedValue = existingValue + elapsedTimeSeconds;

                    // Set the updated value back to the database
                    activityRef.setValue(updatedValue + " sec")
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Connect To Internet : " + e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // If no existing value, simply set the new value
                    activityRef.setValue((int) elapsedTimeSeconds + " sec")
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(context, "Connect To Internet : " + e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("ErrorDB", databaseError.toString());
            }
        });
    }

    private static double millisecondsToMinutes(double milliseconds) {
        return milliseconds / 1000;
    }
    public void Stop() {
        // Call this method from your activity's onStop to handle last data sending
        stopTrackingAndPrint((Activity) context);
    }
}
