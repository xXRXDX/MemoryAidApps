package com.example.memoryaidapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class DismissReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Simple feedback when user dismisses notification
        Toast.makeText(context, "Reminder dismissed", Toast.LENGTH_SHORT).show();
    }
}
