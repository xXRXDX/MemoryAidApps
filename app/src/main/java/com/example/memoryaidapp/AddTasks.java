package com.example.memoryaidapp;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddTasks extends AppCompatActivity {

    private EditText editCustomTask, editDescription, editSchedule;
    private TextView btnHigh, btnMedium, btnLow;
    private Button btnCancel, btnGenerate;

    private Calendar selectedCalendar = Calendar.getInstance();
    private String selectedPriority = "Medium";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_tasks);

        editCustomTask = findViewById(R.id.editCustomTask);
        editDescription = findViewById(R.id.editDescription);
        editSchedule = findViewById(R.id.editSchedule);

        btnHigh = findViewById(R.id.btnHigh);
        btnMedium = findViewById(R.id.btnMedium);
        btnLow = findViewById(R.id.btnLow);

        btnCancel = findViewById(R.id.btnCancel);
        btnGenerate = findViewById(R.id.btnGenerate);

        // initial UI
        editCustomTask.setTextColor(Color.BLACK);
        editDescription.setTextColor(Color.BLACK);
        editSchedule.setTextColor(Color.BLACK);

        // default priority selection
        applyPrioritySelection("Medium");

        // priority click handlers
        btnHigh.setOnClickListener(v -> applyPrioritySelection("High"));
        btnMedium.setOnClickListener(v -> applyPrioritySelection("Medium"));
        btnLow.setOnClickListener(v -> applyPrioritySelection("Low"));

        // schedule input opens date then time picker
        editSchedule.setOnClickListener(v -> openDateTimePicker());

        btnCancel.setOnClickListener(v -> finish());

        btnGenerate.setOnClickListener(v -> {
            String title = editCustomTask.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show();
                return;
            }
            scheduleAlarmForTask(title, editDescription.getText().toString().trim());
            Toast.makeText(this, "Task scheduled", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void applyPrioritySelection(String priority) {
        selectedPriority = priority;

        // highlight selected with system holo colors for clarity, undo for others
        if (priority.equals("High")) {
            btnHigh.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            btnMedium.setBackgroundColor(Color.TRANSPARENT);
            btnLow.setBackgroundColor(Color.TRANSPARENT);
        } else if (priority.equals("Medium")) {
            btnHigh.setBackgroundColor(Color.TRANSPARENT);
            btnMedium.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
            btnLow.setBackgroundColor(Color.TRANSPARENT);
        } else {
            btnHigh.setBackgroundColor(Color.TRANSPARENT);
            btnMedium.setBackgroundColor(Color.TRANSPARENT);
            btnLow.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        }

        // text color contrast
        btnHigh.setTextColor(priority.equals("High") ? Color.BLACK : Color.BLACK);
        btnMedium.setTextColor(priority.equals("Medium") ? Color.BLACK : Color.BLACK);
        btnLow.setTextColor(priority.equals("Low") ? Color.BLACK : Color.BLACK);
    }

    private void openDateTimePicker() {
        final Calendar now = Calendar.getInstance();

        DatePickerDialog dpd = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    // after date picked, show time picker
                    TimePickerDialog tpd = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                selectedCalendar.set(year, month, dayOfMonth, hourOfDay, minute, 0);
                                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                editSchedule.setText(fmt.format(selectedCalendar.getTime()));
                            },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            true);
                    tpd.show();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));
        dpd.show();
    }

    private void scheduleAlarmForTask(String title, String description) {
        Intent intent = new Intent(this, TaskAlarmReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("description", description);
        intent.putExtra("priority", selectedPriority);

        int requestCode = (int) (System.currentTimeMillis() & 0xFFFFFF);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long triggerAt = selectedCalendar.getTimeInMillis();
        if (triggerAt <= System.currentTimeMillis()) {
            // if user didn't set or set to past, default to 5 seconds from now
            triggerAt = System.currentTimeMillis() + 5000;
        }
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent);
        }
    }
}
