package com.example.memoryaidapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class StopwatchActivity extends AppCompatActivity {

    private TextView tvTitle, tvTimer;
    private Button btnTakePic, btnMarkDone;
    private long startTime = 0L;
    private long totalElapsedBeforePause = 0L;
    private boolean running = false;
    private boolean photoTaken = false;
    private long taskId = -1L;

    private android.os.Handler handler = new android.os.Handler();

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            long elapsed = totalElapsedBeforePause + (running ? (System.currentTimeMillis() - startTime) : 0);
            int secs = (int) (elapsed / 1000);
            int hrs = secs / 3600;
            int mins = (secs % 3600) / 60;
            int s = secs % 60;
            tvTimer.setText(String.format("%02d:%02d:%02d", hrs, mins, s));
            handler.postDelayed(this, 500);
        }
    };

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result != null && result.getResultCode() == RESULT_OK) {
                    photoTaken = true;
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopwatch);

        // show over lock screen and keep screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        tvTitle = findViewById(R.id.tvReminderTitle);
        tvTimer = findViewById(R.id.tvReminderTimer);
        btnTakePic = findViewById(R.id.btnTakePic);
        btnMarkDone = findViewById(R.id.btnMarkDone);

        Intent i = getIntent();
        taskId = i.getLongExtra("taskId", -1L);
        String title = i.getStringExtra("title");
        if (title == null) title = "Task";

        tvTitle.setText(title);
        tvTitle.setTextColor(Color.WHITE);
        tvTimer.setTextColor(Color.parseColor("#FFCC00"));

        startStopwatch();

        btnTakePic.setOnClickListener(v -> {
            Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cam.resolveActivity(getPackageManager()) != null) {
                cameraLauncher.launch(cam);
            }
        });

        btnMarkDone.setOnClickListener(v -> {
            stopStopwatch();
            // compute XP (1 xp per 5 seconds, min 1, x2 if photo)
            long elapsed = totalElapsedBeforePause;
            int seconds = (int) (elapsed / 1000);
            int xp = Math.max(1, seconds / 5);
            if (photoTaken) xp *= 2;

            // remove task and cancel alarm
            if (taskId != -1L) {
                TaskStore.removeTaskById(this, taskId);
                cancelAlarmForTask(taskId);
            }

            // show simple popup (Toast) — you can swap for AlertDialog or animated view
            android.widget.Toast.makeText(this, "You earned " + xp + " XP", android.widget.Toast.LENGTH_LONG).show();
            finish();
        });
    }

    private void startStopwatch() {
        startTime = System.currentTimeMillis();
        running = true;
        handler.post(tick);
    }

    private void stopStopwatch() {
        if (running) {
            totalElapsedBeforePause += System.currentTimeMillis() - startTime;
            running = false;
            handler.removeCallbacks(tick);
        }
    }

    private void cancelAlarmForTask(long id) {
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, TaskAlarmReceiver.class);
        intent.putExtra("taskId", id);
        intent.putExtra("title", ""); // extras don't matter for cancel; ensure matching requestCode
        int req = Math.abs((int) (id & 0x7fffffff));
        PendingIntent pi = PendingIntent.getBroadcast(this, req, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (am != null) {
            am.cancel(pi);
        }
    }

    @Override
    public void onBackPressed() {
        // block back to enforce marking done (user can still Home/swap apps)
    }
}
