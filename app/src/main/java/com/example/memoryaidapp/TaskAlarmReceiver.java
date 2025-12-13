package com.example.memoryaidapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class TaskAlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "TASK_ALARM_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {

        long taskId = intent.getLongExtra("taskId", -1);
        String title = intent.getStringExtra("title");
        String desc = intent.getStringExtra("desc");
        if (title == null) title = "Task";
        if (desc == null) desc = "";

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "Task Alarms", NotificationManager.IMPORTANCE_HIGH);
            ch.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), null);
            ch.enableVibration(true);
            nm.createNotificationChannel(ch);
        }

        // Full screen intent opens StopwatchActivity and passes taskId
        Intent full = new Intent(context, StopwatchActivity.class);
        full.putExtra("taskId", taskId);
        full.putExtra("title", title);
        full.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent fullPending = PendingIntent.getActivity(context,
                Math.abs((int) (taskId & 0x7fffffff)),
                full,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder nb = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("⏰ " + title)
                .setContentText(desc)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setFullScreenIntent(fullPending, true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));

        Notification n = nb.build();
        nm.notify(Math.abs((int) (taskId & 0x7fffffff)), n);
    }
}
