package com.example.memoryaidapp;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TaskStore {

    private static final String PREF = "TASK_STORE_V1";
    private static final String KEY = "tasks_json";

    public static void saveTask(Context ctx, Task t) {
        try {
            SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
            JSONArray arr = new JSONArray(sp.getString(KEY, "[]"));

            JSONObject o = new JSONObject();
            o.put("id", t.id);
            o.put("title", t.title);
            o.put("description", t.description);
            o.put("priority", t.priority);
            o.put("timeText", t.timeText);
            o.put("scheduledMillis", t.scheduledMillis);

            arr.put(o);
            sp.edit().putString(KEY, arr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Task> getTasks(Context ctx) {
        ArrayList<Task> list = new ArrayList<>();
        try {
            SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
            JSONArray arr = new JSONArray(sp.getString(KEY, "[]"));

            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                Task t = new Task(
                        o.getLong("id"),
                        o.getString("title"),
                        o.getString("description"),
                        o.getString("priority"),
                        o.getString("timeText"),
                        o.optLong("scheduledMillis", 0L)
                );
                list.add(t);
            }

            // sort by scheduledMillis ascending (soonest first)
            Collections.sort(list, Comparator.comparingLong(a -> a.scheduledMillis));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static boolean removeTaskById(Context ctx, long id) {
        try {
            SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
            JSONArray arr = new JSONArray(sp.getString(KEY, "[]"));
            JSONArray out = new JSONArray();
            boolean removed = false;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                if (o.getLong("id") == id) {
                    removed = true;
                    continue;
                }
                out.put(o);
            }
            sp.edit().putString(KEY, out.toString()).apply();
            return removed;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
