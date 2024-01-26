package com.example.calorieguard;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class MyWorker extends Worker {

    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        int jobType = getInputData().getInt("JobType", 0);

        // Create and show notifications based on jobType
        showNotification(jobType);

        return Result.success();
    }

    private void showNotification(int jobType) {
        String title;
        String content;

        switch (jobType) {
            case 1:
                title = "Good Morning! ☀️";
                content = "Time to log your delicious breakfast. Don't miss out on recording the important meal of the day!🍳🥞";
                break;
            case 2:
                title = "Time for Lunch! 🍲";
                content = "Don't forget to add what you had for lunch🍔🥗.";
                break;
            case 3:
                title = "Dinner Time! 🍽️";
                content = "Capture your dinner moments. Tell us what you enjoyed tonight with just a tap! 🍕🍝";
                break;
            case 4:
                title = "Snack Break! 🍿";
                content = "Time to tell us about those scrumptious snacks you snacked on today. Ready, set, munch! 🍫🥨";
                break;
            default:
                title = "Default Title";
                content = "Default Content";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "calorieGuard")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(jobType, builder.build());
    }
}
