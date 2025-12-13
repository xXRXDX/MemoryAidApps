package com.example.memoryaidapp;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTasks extends AppCompatActivity {

    private EditText editCustomTask, editDescription, editSchedule;
    private TextView btnHigh, btnMedium, btnLow;
    private Button btnCancel, btnGenerate;

    private Calendar selectedCalendar = Calendar.getInstance();
    private String selectedPriority = "Medium";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tasks);

        // request notification permission on Android 13+
        requestNotificationPermissionIfNeeded();

        editCustomTask = findViewById(R.id.editCustomTask);
        editDescription = findViewById(R.id.editDescription);
        editSchedule = findViewById(R.id.editSchedule);

        btnHigh = findViewById(R.id.btnHigh);
        btnMedium = findViewById(R.id.btnMedium);
        btnLow = findViewById(R.id.btnLow);

        btnCancel = findViewById(R.id.btnCancel);
        btnGenerate = findViewById(R.id.btnGenerate);

        // priorities
        btnHigh.setOnClickListener(v -> applyPriority("High"));
        btnMedium.setOnClickListener(v -> applyPriority("Medium"));
        btnLow.setOnClickListener(v -> applyPriority("Low"));

        editSchedule.setOnClickListener(v -> openDateTimePicker());

        btnCancel.setOnClickListener(v -> finish());

        btnGenerate.setOnClickListener(v -> onGenerateClicked());
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 2001);
            }
        }
    }

    private void applyPriority(String p) {
        selectedPriority = p;
        // visual changes: simple background color swap (caller drawables preferred)
        int selHigh = getResources().getColor(android.R.color.holo_red_light);
        int selMed = getResources().getColor(android.R.color.holo_orange_light);
        int selLow = getResources().getColor(android.R.color.holo_green_light);

        btnHigh.setBackgroundColor(p.equals("High") ? selHigh : getResources().getColor(android.R.color.transparent));
        btnMedium.setBackgroundColor(p.equals("Medium") ? selMed : getResources().getColor(android.R.color.transparent));
        btnLow.setBackgroundColor(p.equals("Low") ? selLow : getResources().getColor(android.R.color.transparent));
    }

    private void openDateTimePicker() {
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(this, (v, year, month, day) ->
                new TimePickerDialog(this, (tv, hour, minute) -> {
                    selectedCalendar.set(year, month, day, hour, minute, 0);
                    String formatted = new SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())
                            .format(selectedCalendar.getTime());
                    editSchedule.setText(formatted);
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show()
                , now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void onGenerateClicked() {
        String title = editCustomTask.getText().toString().trim();
        String desc = editDescription.getText().toString().trim();
        String timeText = editSchedule.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Enter task title", Toast.LENGTH_SHORT).show();
            return;
        }

        long id = System.currentTimeMillis();
        long scheduledMillis = selectedCalendar.getTimeInMillis();
        if (scheduledMillis <= 0) {
            // default to 5 seconds from now if not set
            scheduledMillis = System.currentTimeMillis() + 5000;
        }

        Task t = new Task(id, title, desc, selectedPriority, timeText, scheduledMillis);
        TaskStore.saveTask(this, t);

        scheduleAlarmForTask(t);

        // go back to MyTasks so user sees card
        startActivity(new Intent(this, MyTasks.class));
        finish();
    }

    private void scheduleAlarmForTask(Task t) {
        Intent i = new Intent(this, TaskAlarmReceiver.class);
        i.putExtra("taskId", t.id);
        i.putExtra("title", t.title);
        i.putExtra("desc", t.description);

        int req = Math.abs((int) (t.id & 0x7fffffff));

        PendingIntent pi = PendingIntent.getBroadcast(this, req, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (am != null) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, t.scheduledMillis, pi);
        }
    }
}
