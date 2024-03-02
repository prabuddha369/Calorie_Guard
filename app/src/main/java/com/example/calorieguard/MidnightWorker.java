package com.example.calorieguard;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.common.reflect.TypeToken;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MidnightWorker extends Worker {

    private final Context context;

    public MidnightWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Result doWork() {
        // Perform the desired action with the email parameter
        performMidnightAction();

        return Result.success();
    }

    private void performMidnightAction() {
            DBHelper dbHelper = new DBHelper(context);
            dbHelper.dropItemsTable();
    }

}