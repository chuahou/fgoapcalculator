package dev.chuahou.fgo_ap_reminder;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {

    // required views from this activity
    private TextView _outputTextView;
    private EditText _currentAPEditText;
    private EditText _desiredAPEditText;
    private EditText _maxAPEditText;

    // shared preferences for saving and keys
    private SharedPreferences _sharedPreferences;
    private final String _SP_TIME_KEY = "time";
    private final String _SP_CURRENT_AP_KEY = "current";
    private final String _SP_DESIRED_AP_KEY = "desired";
    private final String _SP_MAX_AP_KEY = "max";

    // initialize the activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set content and title
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

        // get needed views
        _outputTextView = (TextView) findViewById(R.id.outputTextView);
        _currentAPEditText = (EditText) findViewById(R.id.currentAPEditText);
        _desiredAPEditText = (EditText) findViewById(R.id.desiredAPEditText);
        _maxAPEditText = (EditText) findViewById(R.id.maxAPEditText);

        // get shared preferences
        _sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(_sharedPreferences.getLong(_SP_TIME_KEY, 0));
        int currentAP = _sharedPreferences.getInt(_SP_CURRENT_AP_KEY, 0);
        int desiredAP = _sharedPreferences.getInt(_SP_DESIRED_AP_KEY, 0);
        int maxAP = _sharedPreferences.getInt(_SP_MAX_AP_KEY, 0);

        // set output text and fill fields
        updateState(c, currentAP, desiredAP, maxAP, false);
        _currentAPEditText.setText(String.valueOf(currentAP));
        _desiredAPEditText.setText(String.valueOf(desiredAP));
        _maxAPEditText.setText(String.valueOf(maxAP));
    }

    // generates a string representing duration from minutes provided
    private String generateDurationString(int minutes)
    {
        StringBuilder sb = new StringBuilder("");

        // 0 minutes
        if (minutes == 0)
        {
            sb.append("0分");
        }

        // hours and minutes
        else if (minutes >= 60)
        {
            // add hours display
            int hours = minutes / 60;
            sb.append(hours + "時間");
            minutes -= hours * 60;

            // add minutes display if necessary
            if (minutes > 0)
                sb.append(String.format("%02d", minutes) + "分");
        }

        // less than 1 hour
        else
        {
            sb.append(minutes + "分");
        }

        return sb.toString();
    }

    // process AP levels, updating the saved state, notification and output text
    private void updateState(Calendar time, int currentAP, int desiredAP, int maxAP, boolean notify)
    {
        // calculate how much time to each AP level
        int minToDesiredAP = 5 * Math.max(desiredAP - currentAP, 0);
        int minToMaxAP = 5 * Math.max(maxAP - currentAP, 0);

        // variable to append to
        StringBuilder outputText = new StringBuilder("");

        // date formatting
        SimpleDateFormat df = new SimpleDateFormat("h:mm a");

        // get current time
        long currTimeInMillis = time.getTimeInMillis(); // for saving
        outputText.append(getString(R.string.currentTime) + df.format(time.getTime()) + "\n");

        // get time to desired AP
        time.add(Calendar.MINUTE, minToDesiredAP);
        long desiredAPTimeInMillis = time.getTimeInMillis();

        // append desired AP text
        outputText.append(getString(R.string.timeToDesired));
        outputText.append(generateDurationString(minToDesiredAP) + "\n");
        outputText.append(getString(R.string.timeAtDesired) + df.format(time.getTime()) + "\n");

        // get time to max AP
        time.add(Calendar.MINUTE, minToMaxAP - minToDesiredAP);
        long maxAPTimeInMillis = time.getTimeInMillis();

        // append max AP text
        outputText.append(getString(R.string.timeToDesired));
        outputText.append(generateDurationString(minToMaxAP) + "\n");
        outputText.append(getString(R.string.timeAtMax) + df.format(time.getTime()) + "\n");

        // set text view text
        _outputTextView.setText(outputText.toString());

        // schedule notifications
        if (notify) {
            Bitmap saber_stand = BitmapFactory.decodeResource(getResources(), R.drawable.saber_stand);
            Bitmap saber_sad = BitmapFactory.decodeResource(getResources(), R.drawable.saber_sad);
            NotificationCompat.Builder builder1 = new NotificationCompat.Builder(this, "0")
                    .setSmallIcon(R.drawable.saber_notif)
                    .setLargeIcon(saber_stand)
                    .setContentTitle(getString(R.string.desiredNotifTitle))
                    .setContentText(getString(R.string.desiredNotifText))
                    .setWhen(desiredAPTimeInMillis);
            scheduleNotification(1, builder1.build(), minToDesiredAP);
            NotificationCompat.Builder builder2 = new NotificationCompat.Builder(this, "0")
                    .setSmallIcon(R.drawable.saber_notif)
                    .setLargeIcon(saber_sad)
                    .setContentTitle(getString(R.string.maxNotifTitle))
                    .setContentText(getString(R.string.maxNotifText))
                    .setWhen(maxAPTimeInMillis);
            scheduleNotification(2, builder2.build(), minToMaxAP);
        }

        // save shared preferences
        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putLong(_SP_TIME_KEY, currTimeInMillis);
        editor.putInt(_SP_CURRENT_AP_KEY, currentAP);
        editor.putInt(_SP_DESIRED_AP_KEY, desiredAP);
        editor.putInt(_SP_MAX_AP_KEY, maxAP);
        editor.commit();
    }

    // handle confirm button clicked
    public void buttonClicked(View view) {
        // get AP values
        int currentAP, desiredAP, maxAP;
        try
        {
            currentAP = Integer.parseInt(_currentAPEditText.getText().toString());
            desiredAP = Integer.parseInt(_desiredAPEditText.getText().toString());
            maxAP = Integer.parseInt(_maxAPEditText.getText().toString());
        } catch (Exception e)
        {
            _outputTextView.setText(R.string.invalidInputMessage);
            return;
        }

        // update state and notify
        updateState(Calendar.getInstance(), currentAP, desiredAP, maxAP, true);
    }

    // schedules provided notification in specified number of minutes
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
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
    }

}
