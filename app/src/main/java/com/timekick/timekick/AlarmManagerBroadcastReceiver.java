package com.timekick.timekick;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import java.util.Calendar;
import java.util.Date;
import com.timekick.service.TTS;

/**
 * Created by pbeyer on 3/5/16.
 */
public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {

    public static int TIME_MODE_ID = 0;
    public static int TARGET_MODE_ID = 1;
    public static int COUNTDOWN_MODE_ID = 2;
    public static int FINAL_MODE_ID = 4;

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "YOUR TAG");
        // Acquire the lock
        wl.acquire();

        speakText(context,intent);

        // Release the lock
        wl.release();
    }

    public Boolean isReminderRunning(Context context, int mode) {
        boolean alarmUp = (PendingIntent.getBroadcast(context, mode,
                new Intent(context, AlarmManagerBroadcastReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);
        return alarmUp;
    }

    public Boolean isAnyReminderRunning(Context context) {
        boolean time = (PendingIntent.getBroadcast(context, TIME_MODE_ID,
                new Intent(context,AlarmManagerBroadcastReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);
        boolean countdown = (PendingIntent.getBroadcast(context, COUNTDOWN_MODE_ID,
                new Intent(context,AlarmManagerBroadcastReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);
        boolean target = (PendingIntent.getBroadcast(context, TARGET_MODE_ID,
                new Intent(context,AlarmManagerBroadcastReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);

        return (time || countdown || target);
    }

    public void SetTimeReminder(Context context, long repeatInterval) {

        AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, TIME_MODE_ID, intent, 0);

        int delay = 60 - Calendar.getInstance().get(Calendar.SECOND);

        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay * 1000,
                repeatInterval * 1000, pi);
    }

    public void SetCountdownReminder(Context context, long repeatInterval, long target) {
        AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        intent.putExtra("MODE","COUNTDOWN");
        intent.putExtra("TARGET",target);
        PendingIntent pi = PendingIntent.getBroadcast(context, COUNTDOWN_MODE_ID, intent, 0);

        am.setRepeating(AlarmManager.RTC_WAKEUP,  System.currentTimeMillis() + repeatInterval * 1000,
                repeatInterval * 1000, pi);

        SetFinalCountdownReminder(context, target);
    }

    public void SetTargetReminder(Context context, long repeatInterval, long target) {
        AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        intent.putExtra("MODE","TARGET");
        intent.putExtra("TARGET",target);
        PendingIntent pi = PendingIntent.getBroadcast(context, TARGET_MODE_ID, intent, 0);

        Calendar c = Calendar.getInstance();
        long current_time = c.getTimeInMillis();
        int delay = 60 - c.get(Calendar.SECOND);
        long m_delay = (delay*1000) + c.get(Calendar.MILLISECOND);
        Date delayedDate = new Date(current_time + m_delay);

        c.setTime(delayedDate);
        c.add(Calendar.SECOND, 30);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        Log.e("ALARM","Repeating alarm will start at: " + c.getTime());

        am.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),
                repeatInterval * 1000, pi);

        SetFinalCountdownReminder(context,target);
    }

    public void SetFinalCountdownReminder(Context context, long target) {
        AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        intent.putExtra("MODE","FINAL");
        intent.putExtra("TARGET",target);
        PendingIntent pi = PendingIntent.getBroadcast(context, FINAL_MODE_ID, intent, 0);

        //Start 10 seconds before target
        am.setRepeating(AlarmManager.RTC_WAKEUP,  target - (10*1000),
                1000, pi);
    }

    public void CancelAlarm(Context context) {
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent sender = PendingIntent
                .getBroadcast(context, TIME_MODE_ID, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        sender.cancel();

        //Cancel Countdown
        sender = PendingIntent
                .getBroadcast(context, COUNTDOWN_MODE_ID, intent, 0);
        alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        sender.cancel();

        //Cancel Target
        sender = PendingIntent
                .getBroadcast(context, TARGET_MODE_ID, intent, 0);
        alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        sender.cancel();

        //Cancel Final
        sender = PendingIntent
                .getBroadcast(context, FINAL_MODE_ID, intent, 0);
        alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        sender.cancel();
    }

    public void speakText(Context context, Intent alarmIntent) {
        Intent speechIntent = new Intent(context, TTS.class);
        speechIntent.putExtras(alarmIntent);
        context.startService(speechIntent);
    }
}
