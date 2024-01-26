package com.example.calorieguard;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.concurrent.ExecutionException;

public class MidnightWorker extends Worker {

    private Context context;

    public MidnightWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Result doWork() {
        // Retrieve the email parameter
        String email = getInputData().getString("email");

        // Perform the desired action with the email parameter
        performMidnightAction(email);

        return Result.success();
    }

    private void performMidnightAction(String email) {
//        DatabaseReference mDatabase = FirebaseDatabase.getInstance("https://calorie-guard-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");

        DBHelper dbHelper = new DBHelper(context);
        dbHelper.dropItemsTable();
    }
}