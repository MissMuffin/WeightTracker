package de.muffinworks.weighttracker.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

/**
 * Just a basic class to keep track of some settings.
 * Created by tethik on 17/04/16.
 */
public class ConfigUtil {


    private SharedPreferences prefs;

    public ConfigUtil(Context context) {
        this.prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    public String getTimePeriod() {
        return this.prefs.getString("timeperiod", "Week");
    }

    public void setTimePeriod(String timePeriod) {
        SharedPreferences.Editor editor = this.prefs.edit();
        editor.putString("timeperiod", timePeriod);
        editor.apply();
    }
}
