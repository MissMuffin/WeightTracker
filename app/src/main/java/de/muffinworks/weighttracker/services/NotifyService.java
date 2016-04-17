package de.muffinworks.weighttracker.services;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import de.muffinworks.weighttracker.MainActivity;
import de.muffinworks.weighttracker.R;

/**
 * Created by tethik on 17/04/16.
 */
public class NotifyService extends Service  {

    public void createNotification() {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_alarm_white_24dp)
                .setContentText("Time to track your weight!")
                .setContentTitle("Weighttracker")
                .setLights(0x0000ff, 3000, 3000)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH);

        Intent resultIntent = new Intent(this, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(this.getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        Notification notification = builder.build();

        NotificationManager mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNM.notify(0, notification);

        Log.i("NotifyService", "Notification sent.");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressWarnings("static-access")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("NotifyService", "Got start command.");
        createNotification();
        return START_NOT_STICKY;
    }


}
