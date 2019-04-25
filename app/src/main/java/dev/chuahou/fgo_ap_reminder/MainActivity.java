package dev.chuahou.fgo_ap_reminder;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle(R.string.actionBarTitle);

        // create notification channel
        CharSequence name = getString(R.string.notificationChannel);
        String description = getString(R.string.notificationChannel);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("0", name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    public void buttonClicked(View view) {
        // get relevant UI components
        TextView outputTextView = (TextView) findViewById(R.id.outputTextView);
        EditText currentAPEditText = (EditText) findViewById(R.id.currentAPEditText);
        EditText desiredAPEditText = (EditText) findViewById(R.id.desiredAPEditText);
        EditText maxAPEditText = (EditText) findViewById(R.id.maxAPEditText);

        // get AP values
        int currentAP, desiredAP, maxAP;
        try
        {
            currentAP = Integer.parseInt(currentAPEditText.getText().toString());
            desiredAP = Integer.parseInt(desiredAPEditText.getText().toString());
            maxAP = Integer.parseInt(maxAPEditText.getText().toString());
        } catch (Exception e)
        {
            outputTextView.setText(R.string.invalidInputMessage);
            return;
        }

        // calculate how much time to each AP level
        int minToDesiredAP = 5 * Math.max(desiredAP - currentAP, 0);
        int minToMaxAP = 5 * Math.max(maxAP - currentAP, 0);

        // variable to append to
        StringBuilder outputText = new StringBuilder("");

        // date formatting
        SimpleDateFormat df = new SimpleDateFormat("h:mm a");

        // get current time
        Calendar c = Calendar.getInstance();
        outputText.append(getString(R.string.currentTime) + df.format(c.getTime()) + "\n");

        // get time to desired AP
        c.add(Calendar.MINUTE, minToDesiredAP);
        int dispHours, dispMins; // displayed hours and minutes
        dispHours = minToDesiredAP / 60;
        dispMins = minToDesiredAP - dispHours * 60;
        outputText.append(getString(R.string.timeToDesired));
        if (dispHours > 0)
            outputText.append(dispHours + "時間");
        outputText.append(dispMins + "分\n");
        outputText.append(getString(R.string.timeAtDesired) + df.format(c.getTime()) + "\n");

        // get time to max AP
        c.add(Calendar.MINUTE, minToMaxAP - minToDesiredAP);
        dispHours = minToMaxAP / 60;
        dispMins = minToMaxAP - dispHours * 60;
        outputText.append(getString(R.string.timeToDesired));
        if (dispHours > 0)
            outputText.append(dispHours + "時間");
        outputText.append(dispMins + "分\n");
        outputText.append(getString(R.string.timeAtMax) + df.format(c.getTime()) + "\n");

        // set text view text
        outputTextView.setText(outputText.toString());

        // schedule notifications
        Bitmap saber_stand = BitmapFactory.decodeResource(getResources(), R.drawable.saber_stand);
        Bitmap saber_sad = BitmapFactory.decodeResource(getResources(), R.drawable.saber_sad);
        NotificationCompat.Builder builder1 = new NotificationCompat.Builder(this, "0")
                .setSmallIcon(R.drawable.saber_notif)
                .setLargeIcon(saber_stand)
                .setContentTitle(getString(R.string.desiredNotifTitle))
                .setContentText(getString(R.string.desiredNotifText));
        scheduleNotification(1, builder1.build(), minToDesiredAP);
        NotificationCompat.Builder builder2 = new NotificationCompat.Builder(this, "0")
                .setSmallIcon(R.drawable.saber_notif)
                .setLargeIcon(saber_sad)
                .setContentTitle(getString(R.string.maxNotifTitle))
                .setContentText(getString(R.string.maxNotifText));
        scheduleNotification(2, builder2.build(), minToMaxAP);
    }

    private void scheduleNotification(int id, Notification n, int delay) {
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, id);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, n);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id,
                notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Calendar time = Calendar.getInstance();
        time.setTimeInMillis(System.currentTimeMillis());
        time.add(Calendar.MINUTE, delay);
        alarmManager.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
    }

}
