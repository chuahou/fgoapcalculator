package dev.chuahou.fgo_ap_reminder;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class NotificationPublisher extends BroadcastReceiver
{

    public static String NotificationId = "notification-id";
    public static String NotificationExtra = "notification";

    /**
     * Schedules provided notification in specified number of minutes.
     *
     * @param context Context of the application package
     * @param id notification ID
     * @param n notification to schedule
     * @param delay delay in minutes
     */
    public static void ScheduleNotificationInMinutes(Context context, int id,
                                               Notification n, int delay)
    {
        Intent notificationIntent =
                new Intent(context, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationId, id);
        notificationIntent.putExtra(NotificationExtra, n);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(System.currentTimeMillis());
        time.add(Calendar.MINUTE, delay);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(),
                pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(
                        Context.NOTIFICATION_SERVICE);

        Notification notification = intent.getParcelableExtra(NotificationExtra);
        int id = intent.getIntExtra(NotificationId, 0);
        notificationManager.notify(id, notification);
    }

}
