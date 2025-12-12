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

        if (home == null) return; // Page has no nav

        home.setOnClickListener(v ->
                activity.startActivity(new Intent(activity, Home.class))
        );

        tasks.setOnClickListener(v ->
                activity.startActivity(new Intent(activity, MyTasks.class))
        );

        streaks.setOnClickListener(v ->
                activity.startActivity(new Intent(activity, Streak.class))
        );

        profile.setOnClickListener(v ->
                activity.startActivity(new Intent(activity, Wardrobe.class))
        );

        community.setOnClickListener(v ->
                activity.startActivity(new Intent(activity, Community.class))
        );

        timer.setOnClickListener(v ->
                activity.startActivity(new Intent(activity, Timer.class))
        );
    }
}
