package dev.chuahou.fgo_ap_reminder;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity
{

    // required views from this activity
    private TextView _outputTextView;
    private EditText _currentApEditText;
    private EditText _desiredApEditText;
    private EditText _maxApEditText;
    private TextView _projectedAPTextView;
    private ProgressBar _progressBar;

    // shared preferences for saving and keys
    private SharedPreferences _sharedPreferences;
    private final String _SP_TIME_KEY = "time";
    private final String _SP_CURRENT_AP_KEY = "current";
    private final String _SP_DESIRED_AP_KEY = "desired";
    private final String _SP_MAX_AP_KEY = "max";

    // initialize the activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // set content and title
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(R.string.actionBarTitle);

        // create notification channel
        CharSequence name = getString(R.string.notificationChannel);
        String description = getString(R.string.notificationChannel);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel =
                new NotificationChannel("0", name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager =
                getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        // get needed views
        _outputTextView = findViewById(R.id.outputTextView);
        _currentApEditText = findViewById(R.id.currentApEditText);
        _desiredApEditText = findViewById(R.id.desiredApEditText);
        _maxApEditText = findViewById(R.id.maxApEditText);
        _projectedAPTextView = findViewById(R.id.projectedApTextView);
        _progressBar = findViewById(R.id.progressBar);
    }

    // reloads shared preferences and refreshes UI
    @Override
    protected void onResume()
    {
        super.onResume();

        // get shared preferences
        _sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(_sharedPreferences.getLong(_SP_TIME_KEY,
                Calendar.getInstance().getTimeInMillis()));
        int currentAp = _sharedPreferences.getInt(_SP_CURRENT_AP_KEY, 0);
        int desiredAp = _sharedPreferences.getInt(_SP_DESIRED_AP_KEY, 40);
        int maxAp = _sharedPreferences.getInt(_SP_MAX_AP_KEY, 120);

        // set output text and fill fields
        UpdateUi((Calendar) c.clone(), currentAp, desiredAp, maxAp);
        UpdateProgressBar((Calendar) c.clone(), currentAp, desiredAp, maxAp);
        SaveSharedPreferences((Calendar) c.clone(), currentAp, desiredAp,
                maxAp);
        _currentApEditText.setText(String.valueOf(currentAp));
        _desiredApEditText.setText(String.valueOf(desiredAp));
        _maxApEditText.setText(String.valueOf(maxAp));
    }

    // handle confirm button clicked
    public void buttonClicked(View view)
    {
        // get AP values
        int currentAp, desiredAp, maxAp;
        try
        {
            currentAp =
                    Integer.parseInt(_currentApEditText.getText().toString());
            desiredAp =
                    Integer.parseInt(_desiredApEditText.getText().toString());
            maxAp = Integer.parseInt(_maxApEditText.getText().toString());
        }
        catch (Exception e)
        {
            _outputTextView.setText(R.string.invalidInputMessage);
            return;
        }

        // update state and notify
        UpdateUi(Calendar.getInstance(), currentAp, desiredAp, maxAp);
        UpdateProgressBar(Calendar.getInstance(), currentAp, desiredAp, maxAp);
        SaveSharedPreferences(Calendar.getInstance(), currentAp, desiredAp,
                maxAp);
        ScheduleNotifications(Calendar.getInstance(), currentAp, desiredAp,
                maxAp);
    }

    /**
     * Generates a string representing the duration provided.
     *
     * @param minutes the duration in minutes
     * @return string format duration
     */
    @SuppressLint("DefaultLocale")
    private String GenerateDurationString(int minutes)
    {
        StringBuilder sb = new StringBuilder();

        // 0 minutes
        if (minutes == 0)
        {
            sb.append("0 min");
        }

        // hours and minutes
        else if (minutes >= 60)
        {
            // add hours display
            int hours = minutes / 60;
            sb.append(hours).append(" h ");
            minutes -= hours * 60;

            // add minutes display if necessary
            if (minutes > 0)
                sb.append(String.format("%02d", minutes)).append(" min");
        }

        // less than 1 hour
        else
        {
            sb.append(minutes).append(" min");
        }

        return sb.toString();
    }

    /**
     * Calculates time taken in between two AP levels.
     *
     * @param fromAp starting AP level
     * @param toAp AP level to calculate time till
     * @return time till second AP level
     */
    private int TimeBetweenApLevels(int fromAp, int toAp)
    {
        return 5 * Math.max(toAp - fromAp, 0);
    }

    /**
     * Process AP levels, updating UI.
     *
     * @param time time at which calculation was performed
     * @param currentAp current AP at time of calculation
     * @param desiredAp desired AP
     * @param maxAp maximum AP
     */
    private void UpdateUi(Calendar time, int currentAp, int desiredAp,
                          int maxAp)
    {
        // calculate how much time to each AP level
        int minToDesiredAp = TimeBetweenApLevels(currentAp, desiredAp);
        int minToMaxAp = TimeBetweenApLevels(currentAp, maxAp);

        // variable to append to
        StringBuilder outputText = new StringBuilder();

        // date formatting
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat df = new SimpleDateFormat("h:mm a");

        // get current time
        outputText.append(getString(R.string.currentTime))
                .append(df.format(time.getTime())).append("\n");

        // get time to desired AP
        time.add(Calendar.MINUTE, minToDesiredAp);

        // append desired AP text
        outputText.append(getString(R.string.timeToDesired))
                .append(GenerateDurationString(minToDesiredAp))
                .append("\n")
                .append(getString(R.string.timeAtDesired))
                .append(df.format(time.getTime()))
                .append("\n");

        // get time to max AP
        time.add(Calendar.MINUTE, minToMaxAp - minToDesiredAp);

        // append max AP text
        outputText.append(getString(R.string.timeToDesired))
                .append(GenerateDurationString(minToMaxAp))
                .append("\n")
                .append(getString(R.string.timeAtMax))
                .append(df.format(time.getTime()))
                .append("\n");

        // set text view text
        _outputTextView.setText(outputText.toString());
    }

    /**
     * Process AP levels, updating progress bar.
     *
     * @param time time at which calculation was performed
     * @param currentAp current AP at time of calculation
     * @param desiredAp desired AP
     * @param maxAp maximum AP
     */
    private void UpdateProgressBar(Calendar time, int currentAp, int desiredAp,
                                   int maxAp)
    {
        // calculate projected AP
        int minSinceCalculation = (int)
                ((Calendar.getInstance().getTimeInMillis() -
                        time.getTimeInMillis()) / (1000 * 60));
        int projectedAp = Math.min(currentAp + (minSinceCalculation / 5),
                maxAp);

        // set projected AP text view
        String outputText = getString(R.string.projectedAp) +
                projectedAp + "/" + maxAp;
        _projectedAPTextView.setText(outputText);

        // set progress bar
        if (maxAp <= 0) maxAp = 1;
        _progressBar.setProgress(projectedAp * 100 / maxAp);
        int progressBarColor;
        if (projectedAp < desiredAp)
            progressBarColor = getColor(R.color.progressBarLow);
        else if (projectedAp < 0.9 * maxAp)
            progressBarColor = getColor(R.color.progressBarDesired);
        else if (projectedAp < maxAp)
            progressBarColor = getColor(R.color.progressBarNearFull);
        else
            progressBarColor = getColor(R.color.progressBarFull);
        _progressBar.setProgressTintList(
                ColorStateList.valueOf(progressBarColor));
    }

    /**
     * Schedule notifications based on AP levels.
     *
     * @param time time at which calculation was performed
     * @param currentAp current AP at time of calculation
     * @param desiredAp desired AP
     * @param maxAp maximum AP
     */
    private void ScheduleNotifications(Calendar time, int currentAp,
                                       int desiredAp, int maxAp)
    {
        // get time of desired AP and max AP
        int minToDesiredAp = TimeBetweenApLevels(currentAp, desiredAp);
        time.add(Calendar.MINUTE, minToDesiredAp);
        long desiredApTimeInMillis = time.getTimeInMillis();
        int minToMaxAp = TimeBetweenApLevels(currentAp, maxAp);
        time.add(Calendar.MINUTE, minToMaxAp - minToDesiredAp);
        long maxApTimeInMillis = time.getTimeInMillis();

        // schedule notifications
        NotificationCompat.Builder builder1 =
                new NotificationCompat.Builder(this, "0")
                        .setSmallIcon(R.drawable.logo_notif)
                        .setContentTitle(getString(R.string.desiredNotifTitle))
                        .setContentText(getString(R.string.desiredNotifText))
                        .setWhen(desiredApTimeInMillis);
        NotificationPublisher.ScheduleNotificationInMinutes(this, 1,
                builder1.build(), minToDesiredAp);
        NotificationCompat.Builder builder2 =
                new NotificationCompat.Builder(this, "0")
                        .setSmallIcon(R.drawable.logo_notif)
                        .setContentTitle(getString(R.string.maxNotifTitle))
                        .setContentText(getString(R.string.maxNotifText))
                        .setWhen(maxApTimeInMillis);
        NotificationPublisher.ScheduleNotificationInMinutes(this, 2,
                builder2.build(), minToMaxAp);
    }

    /**
     * Save current state for later loading.
     *
     * @param time time at which calculation was performed
     * @param currentAp current AP at time of calculation
     * @param desiredAp desired AP
     * @param maxAp maximum AP
     */
    private void SaveSharedPreferences(Calendar time, int currentAp,
                                       int desiredAp, int maxAp)
    {
        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putLong(_SP_TIME_KEY, time.getTimeInMillis());
        editor.putInt(_SP_CURRENT_AP_KEY, currentAp);
        editor.putInt(_SP_DESIRED_AP_KEY, desiredAp);
        editor.putInt(_SP_MAX_AP_KEY, maxAp);
        editor.apply();
    }

}
