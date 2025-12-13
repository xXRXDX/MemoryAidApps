package com.example.memoryaidapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MyTasks extends AppCompatActivity {

    private LinearLayout customTaskContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tasks);

        // THIS MATCHES YOUR XML
        customTaskContainer = findViewById(R.id.customTaskContainer);

        // Optional: wire these later if you want
        Button btnDaily = findViewById(R.id.btnDaily);
        Button btnMain = findViewById(R.id.btnMain);
        Button btnSide = findViewById(R.id.btnSide);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCustomTasks(); // reload every time we return
    }

    private void loadCustomTasks() {
        customTaskContainer.removeAllViews(); // prevent duplicates

        List<Task> tasks = TaskStore.getTasks(this);

        LayoutInflater inflater = LayoutInflater.from(this);

        for (Task task : tasks) {

            View card = inflater.inflate(
                    R.layout.item_custom_task,
                    customTaskContainer,
                    false
            );

            TextView tvTitle = card.findViewById(R.id.tvQuestTitle);
            TextView tvBadge = card.findViewById(R.id.tvBadge);
            TextView tvTime = card.findViewById(R.id.tvTime);
            Button btnAccept = card.findViewById(R.id.btnAcceptQuest);

            tvTitle.setText(task.title);
            tvBadge.setText(task.priority);

            if (task.timeText != null && !task.timeText.isEmpty()) {
                tvTime.setText(task.timeText);
            } else {
                tvTime.setText("No schedule");
            }

            // ACCEPT QUEST → OPEN STOPWATCH
            btnAccept.setOnClickListener(v -> {
                Intent intent = new Intent(MyTasks.this, StopwatchActivity.class);
                intent.putExtra("taskId", task.id);
                intent.putExtra("title", task.title);
                startActivity(intent);
            });

            // ADD CARD INTO SCROLLABLE CONTAINER
            customTaskContainer.addView(card);
        }
    }
}
