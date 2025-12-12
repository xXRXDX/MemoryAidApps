package com.example.memoryaidapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {
    private static final String CHANNEL_ID = "task_reminders_channel";
    private Context ctx;

    public NotificationHelper(Context ctx) {
        this.ctx = ctx;
        createChannel();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Reminders";
            String description = "Reminders for scheduled tasks";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    public void showReminderNotification(String taskId, String title, String text, boolean playSound) {
        // Proceed intent (opens full-screen reminder)
        Intent proceedIntent = new Intent(ctx, Reminder.class);
        proceedIntent.putExtra("taskId", taskId);
        proceedIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent proceedPending = PendingIntent.getActivity(
                ctx,
                Math.abs((taskId + "proceed").hashCode()),
                proceedIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Dismiss action
        Intent dismissIntent = new Intent(ctx, DismissReceiver.class);
        dismissIntent.putExtra("taskId", taskId);
        PendingIntent dismissPending = PendingIntent.getBroadcast(
                ctx,
                Math.abs((taskId + "dismiss").hashCode()),
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // add a small icon in res/drawable
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(new NotificationCompat.Action(0, "Proceed", proceedPending))
                .addAction(new NotificationCompat.Action(0, "Dismiss", dismissPending));

        NotificationManagerCompat nm = NotificationManagerCompat.from(ctx);
        nm.notify(Math.abs(taskId.hashCode()), builder.build());
    }
}
