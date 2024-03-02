package com.example.calorieguard;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.annotation.NonNull;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
        double elapsedTimeMinutes = millisecondsToSeconds((double) elapsedTimeMillis);

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

            // Check if the document already exists
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference documentRef = db.collection("Users")
                    .document(userNode)
                    .collection("Analysis")
                    .document(localTimestamp)
                    .collection("Activities")
                    .document(activityNode);

            documentRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        double existingTime = document.getDouble("time");
                        // Add the existing time to the current elapsedTimeMinutes
                        double updatedTime = existingTime + elapsedTimeMinutes;

                        // Update the "time" field with the new calculated time
                        HashMap<String, Object> data = new HashMap<>();
                        data.put("time", updatedTime);

                        documentRef.set(data, SetOptions.merge());
                    } else {
                        HashMap<String, Object> data = new HashMap<>();
                        data.put("time", elapsedTimeMinutes);
                        // Document already exists, update the existing data
                        documentRef.set(data, SetOptions.merge());
                    }
                } else {
                    // Handle exceptions or errors
                    Toast.makeText(currentActivity, "No Internet Available!", Toast.LENGTH_SHORT).show();
                    Exception exception = task.getException();
                    if (exception != null) {
                        Log.d("Firestore Error",exception.toString());
                    }
                }
            });

        } catch (ParseException e) {
            e.printStackTrace(); // Handle parsing exception if necessary
        }
    }


//
//        // Retrieve existing data from SharedPreferences
//        SharedPreferences sharedPreferences = context.getSharedPreferences("Analytics", Context.MODE_PRIVATE);
//        String existingDataJson = sharedPreferences.getString("analytics_data", "{}");
//
//        try {
//            // Convert existing data JSON to a HashMap
//            Type type = new TypeToken<HashMap<String, HashMap<String, HashMap<String, Object>>>>() {}.getType();
//            HashMap<String, HashMap<String, HashMap<String, Object>>> existingData = new Gson().fromJson(existingDataJson, type);
//
//            // Update or add new data
//            if (!existingData.containsKey(userNode)) {
//                existingData.put(userNode, new HashMap<>());
//                existingData.get(userNode).put(localTimestamp,new HashMap<>());
//                existingData.get(userNode).get(localTimestamp).put(activityNode,Double.toString(elapsedTimeSeconds));
//            }
//            else{
//                if (!existingData.get(userNode).containsKey(localTimestamp)) {
//                    existingData.get(userNode).put(localTimestamp, new HashMap<>());
//                    existingData.get(userNode).get(localTimestamp).put(activityNode,Double.toString(elapsedTimeSeconds));
//                }
//                else {
//                    if(!existingData.get(userNode).get(localTimestamp).containsKey(activityNode))
//                    {
//                        existingData.get(userNode).get(localTimestamp).put(activityNode,elapsedTimeSeconds);
//                    }
//                    else
//                    {
//                        double existing_time = (double) existingData.get(userNode).get(localTimestamp).get(activityNode);
//                        elapsedTimeSeconds+=existing_time;
//                    }
//                }
//            }
//
//            existingData.get(userNode).get(localTimestamp).put(activityNode, elapsedTimeSeconds);
//
//            // Convert the updated data back to JSON
//            String updatedDataJson = new Gson().toJson(existingData);
//
//            // Save the updated data to SharedPreferences
//            SharedPreferences.Editor editor = sharedPreferences.edit();
//            editor.putString("analytics_data", updatedDataJson);
//            editor.apply();
//
//        } catch (JsonSyntaxException e) {
//            Log.d("Error",e.toString());
//        }




//        DatabaseReference activityRef = mDatabase.child(userNode).child("Analysis").child(localTimestamp).child(activityNode);
//
//        // Get the existing value
//        activityRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    // Update the existing value with the new data
//                    double existingValue = Double.parseDouble(Objects.requireNonNull(dataSnapshot.getValue(String.class)).replace(" sec", ""));
//                    double updatedValue = existingValue + elapsedTimeSeconds;
//
//                    // Set the updated value back to the database
//                    activityRef.setValue(updatedValue + " sec")
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Toast.makeText(context, "Connect To Internet : " + e.toString(), Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                } else {
//                    // If no existing value, simply set the new value
//                    activityRef.setValue((int) elapsedTimeSeconds + " sec")
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Toast.makeText(context, "Connect To Internet : " + e.toString(), Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.d("ErrorDB", databaseError.toString());
//            }
//        });

    private static double millisecondsToSeconds(double milliseconds) {
        return milliseconds / 1000;
    }
    public void Stop() {
        // Call this method from your activity's onStop to handle last data sending
        stopTrackingAndPrint((Activity) context);
    }

//    public void sendDataToServer() {
//        SharedPreferences sharedPreferences = context.getSharedPreferences("Analytics", Context.MODE_PRIVATE);
//        String analyticsDataJson = sharedPreferences.getString("analytics_data", "{}");
//
//        try {
//            // Convert data JSON to a HashMap
//            Type type = new com.google.common.reflect.TypeToken<HashMap<String, HashMap<String, HashMap<String, Object>>>>() {}.getType();
//            HashMap<String, HashMap<String, HashMap<String, Object>>> analyticsData = new Gson().fromJson(analyticsDataJson, type);
//
//            // Use the analyticsData HashMap as needed
//
//            // Example: Iterate through userNodes
//            for (Map.Entry<String, HashMap<String, HashMap<String, Object>>> userEntry : analyticsData.entrySet()) {
//                String userNode = userEntry.getKey();
//
//                // Example: Iterate through localTimestamps for each user
//                for (Map.Entry<String, HashMap<String, Object>> timestampEntry : userEntry.getValue().entrySet()) {
//                    String localTimestamp = timestampEntry.getKey();
//
//                    // Example: Iterate through activityNodes for each timestamp
//                    for (Map.Entry<String, Object> activityEntry : timestampEntry.getValue().entrySet()) {
//                        String activityNode = activityEntry.getKey();
//                        Object activityValue = activityEntry.getValue();
//
//                        if (activityValue instanceof Double) {
//                            double time = (double) activityValue;
//                            HashMap<String, Object> data = new HashMap<>();
//                            data.put("time", time);
//
//                            FirebaseFirestore db = FirebaseFirestore.getInstance();
//                            // Update data in Firestore with merge strategy
//                            db.collection("Users")
//                                    .document(userNode)
//                                    .collection("Analysis")
//                                    .document(localTimestamp)
//                                    .collection("Activities")
//                                    .document(activityNode)
//                                    .set(data, SetOptions.merge())
//                                    .addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//                                            // Handle the failure
//                                            Log.e("FirestoreError", "Error adding data: " + e.getMessage());
//                                        }
//                                    });
//                        } else {
//                            // Handle the case where activityValue is not a Double (e.g., log an error)
//                            Log.e("MyWorkerError", "Activity value is not a Double for userNode: " + userNode + ", localTimestamp: " + localTimestamp + ", activityNode: " + activityNode);
//                        }
//                    }
//                }
//            }
//
//        } catch (JsonSyntaxException e) {
//            Log.d("MyWorkerError",e.toString());
//        }
//
//        clearSharedPreferencesData();
//    }
//
//    private void clearSharedPreferencesData() {
//        SharedPreferences sharedPreferences = context.getSharedPreferences("Analytics", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.remove("analytics_data");
//        editor.apply();
//    }

}
