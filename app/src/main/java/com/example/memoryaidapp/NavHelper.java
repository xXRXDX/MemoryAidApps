package com.example.memoryaidapp;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

public class NavHelper {

    public static void setupBottomNav(Activity activity) {

        ImageView home = activity.findViewById(R.id.navHome);
        ImageView tasks = activity.findViewById(R.id.navTasks);
        ImageView streaks = activity.findViewById(R.id.navStreaks);
        ImageView profile = activity.findViewById(R.id.navProfile);
        ImageView community = activity.findViewById(R.id.navCommunity);
        ImageView timer = activity.findViewById(R.id.navTimer);

        // If this screen has no bottom nav, exit safely
        if (home == null || tasks == null || streaks == null ||
                profile == null || community == null || timer == null) {
            return;
        }

        home.setOnClickListener(v -> navigate(activity, Home.class));
        tasks.setOnClickListener(v -> navigate(activity, MyTasks.class));
        streaks.setOnClickListener(v -> navigate(activity, Streak.class));
        profile.setOnClickListener(v -> navigate(activity, Wardrobe.class));
        community.setOnClickListener(v -> navigate(activity, Community.class));
        timer.setOnClickListener(v -> navigate(activity, Timer.class));
    }

    private static void navigate(Activity activity, Class<?> target) {
        // Don’t reload the same screen
        if (activity.getClass().equals(target)) return;

        Intent intent = new Intent(activity, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        activity.startActivity(intent);
    }
}
