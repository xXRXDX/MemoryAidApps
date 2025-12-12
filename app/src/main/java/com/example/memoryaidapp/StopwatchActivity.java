package com.example.memoryaidapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class StopwatchActivity extends AppCompatActivity {

    private TextView tvTitle, tvTimer;
    private Button btnTakePhoto, btnMarkDone;

    private long startTime = 0L;
    private long elapsedBeforePause = 0L;
    private boolean running = false;
    private boolean photoTaken = false;

    private Handler handler = new Handler();

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            long now = System.currentTimeMillis();
            long diff = (running ? (now - startTime) + elapsedBeforePause : elapsedBeforePause);
            int totalSeconds = (int) (diff / 1000);
            int hrs = totalSeconds / 3600;
            int mins = (totalSeconds % 3600) / 60;
            int secs = totalSeconds % 60;
            tvTimer.setText(String.format("%02d:%02d:%02d", hrs, mins, secs));
            handler.postDelayed(this, 500);
        }
    };

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                // if camera app returned a photo (thumbnail) we consider a photo taken
                if (result != null && result.getResultCode() == RESULT_OK) {
                    photoTaken = true;
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopwatch);

        // show over lock screen & keep screen on
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        tvTitle = findViewById(R.id.tvReminderTitle);
        tvTimer = findViewById(R.id.tvReminderTimer);
        btnTakePhoto = findViewById(R.id.btnTakePic);
        btnMarkDone = findViewById(R.id.btnMarkDone);

        String title = getIntent().getStringExtra("title");
        if (title == null) title = "Task";

        tvTitle.setText(title);
        tvTitle.setTextColor(Color.WHITE);
        tvTimer.setTextColor(Color.parseColor("#FFCC00"));

        startStopwatch();

        btnTakePhoto.setOnClickListener(v -> {
            Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cam.resolveActivity(getPackageManager()) != null) {
                cameraLauncher.launch(cam);
            }
        });

        btnMarkDone.setOnClickListener(v -> completeTask());
    }

    private void startStopwatch() {
        startTime = System.currentTimeMillis();
        elapsedBeforePause = 0L;
        running = true;
        handler.post(tick);
    }

    private void stopStopwatch() {
        if (running) {
            elapsedBeforePause += System.currentTimeMillis() - startTime;
            running = false;
            handler.removeCallbacks(tick);
        }
    }

    private void completeTask() {
        stopStopwatch();
        long totalMillis = elapsedBeforePause;
        int seconds = (int) (totalMillis / 1000);

        // base EXP calculation: 1 XP for every 5 seconds
        int xp = seconds / 5;
        if (xp < 1) xp = 1;

        // multiplier if photo taken
        if (photoTaken) xp *= 2;

        // show popup with scaled XP and a simple progress-like scaling message
        String message = "You earned " + xp + " XP!\n\n";
        message += "Time: " + formatTime(totalMillis) + "\n";
        message += "Photo bonus: " + (photoTaken ? "x2 applied" : "none");

        new AlertDialog.Builder(this)
                .setTitle("Task Completed")
                .setMessage(message)
                .setPositiveButton("OK", (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    private String formatTime(long millis) {
        int totalSeconds = (int) (millis / 1000);
        int hrs = totalSeconds / 3600;
        int mins = (totalSeconds % 3600) / 60;
        int secs = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hrs, mins, secs);
    }

    @Override
    public void onBackPressed() {
        // block back button while in full-screen reminder (user must press Mark task as done)
    }
}
