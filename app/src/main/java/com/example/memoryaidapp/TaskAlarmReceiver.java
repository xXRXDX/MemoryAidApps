package com.example.memoryaidapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class TaskAlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "task_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        if (title == null) title = "Scheduled Task";

        // build notification channel
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "Task reminders", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Reminders for scheduled tasks");
            nm.createNotificationChannel(ch);
        }

        // Proceed -> opens StopwatchActivity
        Intent proceed = new Intent(context, StopwatchActivity.class);
        proceed.putExtra("title", title);
        proceed.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent proceedPending = PendingIntent.getActivity(context,
                (int) (System.currentTimeMillis() & 0xFFFFFF),
                proceed,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Dismiss action (fires a small receiver)
        Intent dismiss = new Intent(context, DismissReceiver.class);
        PendingIntent dismissPending = PendingIntent.getBroadcast(context,
                (int) ((System.currentTimeMillis() + 1) & 0xFFFFFF),
                dismiss,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder nb = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText("Tap Proceed to start the task stopwatch")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(0, "Proceed", proceedPending)
                .addAction(0, "Dismiss", dismissPending);

        if (nm != null) {
            nm.notify((int) (System.currentTimeMillis() & 0xFFFFFF), nb.build());
        }

        // Also start StopwatchActivity on receive so the user sees it right away (some devices)
        Intent start = new Intent(context, StopwatchActivity.class);
        start.putExtra("title", title);
        start.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(start);
    }
}
