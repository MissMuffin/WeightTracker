package de.muffinworks.weighttracker.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

import de.muffinworks.weighttracker.db.WeightDbService;
import de.muffinworks.weighttracker.util.DateUtil;

/**
 * Created by tethik on 17/04/16.
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("WeightTrackerAlarm", "Woke up from alarm");
        WeightDbService db = new WeightDbService(context);
        if(db.hasEntryFor(DateUtil.currentDate())) {
            Log.i("WeightTrackerAlarm", "Entry already exists for today, skipping notification.");
            return;
        }

        Intent serviceIntent = new Intent(context, NotifyService.class);
        context.startService(serviceIntent);
    }

    public void setAlarm(Context context, int hour, int minute) {
        Intent myIntent = new Intent(context, AlarmReceiver.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 13371337, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.HOUR_OF_DAY, hour);

        //alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pendingIntent) // 1000 * 60 * 60 * 24;
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        Log.i("AlarmReceiver", "Set up alarm.");
    }

    public void clearAlarm(Context context) {
        Intent myIntent = new Intent(context, AlarmReceiver.class);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 13371337, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);
    }
}
