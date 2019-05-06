package dev.chuahou.fgo_ap_reminder;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationPublisher extends BroadcastReceiver
{

    public static String NotificationId = "notification-id";
    public static String NotificationExtra = "notification";

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
