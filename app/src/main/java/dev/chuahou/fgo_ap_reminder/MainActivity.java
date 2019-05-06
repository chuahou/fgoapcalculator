package dev.chuahou.fgo_ap_reminder;

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
        UpdateState(c, currentAp, desiredAp, maxAp, false);
        _currentApEditText.setText(String.valueOf(currentAp));
        _desiredApEditText.setText(String.valueOf(desiredAp));
        _maxApEditText.setText(String.valueOf(maxAp));
    }

    // initialize the activity
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // set content and title
        setContentView(R.layout.activity_main);
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
        _outputTextView = (TextView) findViewById(R.id.outputTextView);
        _currentApEditText = (EditText) findViewById(R.id.currentAPEditText);
        _desiredApEditText = (EditText) findViewById(R.id.desiredAPEditText);
        _maxApEditText = (EditText) findViewById(R.id.maxAPEditText);
        _projectedAPTextView = (TextView)
                findViewById(R.id.projectedAPTextView);
        _progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    /**
     * Generates a string representing the duration provided.
     *
     * @param minutes the duration in minutes
     * @return string format duration
     */
    private String GenerateDurationString(int minutes)
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

    /**
     * Process AP levels, updating the saved state, notification and output
     * text in the UI.
     *
     * @param time time at which calculation was performed
     * @param currentAp current AP at time of calculation
     * @param desiredAp desired AP
     * @param maxAp maximum AP
     * @param notify true if notification is to be created
     */
    private void UpdateState(Calendar time, int currentAp, int desiredAp,
                             int maxAp, boolean notify)
    {
        // calculate how much time to each AP level
        int minTodesiredAp = 5 * Math.max(desiredAp - currentAp, 0);
        int minTomaxAp = 5 * Math.max(maxAp - currentAp, 0);

        // variable to append to
        StringBuilder outputText = new StringBuilder("");

        // date formatting
        SimpleDateFormat df = new SimpleDateFormat("h:mm a");

        // get current time
        long currTimeInMillis = time.getTimeInMillis(); // for saving
        outputText.append(getString(R.string.currentTime) + df.format(time.getTime()) + "\n");

        // get time to desired AP
        time.add(Calendar.MINUTE, minTodesiredAp);
        long desiredApTimeInMillis = time.getTimeInMillis();

        // append desired AP text
        outputText.append(getString(R.string.timeToDesired));
        outputText.append(GenerateDurationString(minTodesiredAp) + "\n");
        outputText.append(getString(R.string.timeAtDesired) + df.format(time.getTime()) + "\n");

        // get time to max AP
        time.add(Calendar.MINUTE, minTomaxAp - minTodesiredAp);
        long maxApTimeInMillis = time.getTimeInMillis();

        // append max AP text
        outputText.append(getString(R.string.timeToDesired));
        outputText.append(GenerateDurationString(minTomaxAp) + "\n");
        outputText.append(getString(R.string.timeAtMax) +
                df.format(time.getTime()) + "\n");

        // set text view text
        _outputTextView.setText(outputText.toString());

        // calculate projected AP
        int minSinceCalculation = (int)
                ((Calendar.getInstance().getTimeInMillis() - currTimeInMillis) /
                        (1000 * 60));
        int projectedAP = currentAp + (minSinceCalculation / 5);

        // set projected AP text view
        outputText = new StringBuilder();
        outputText.append(getString(R.string.projectedAP));
        outputText.append(projectedAP + "/" + maxAp);
        _projectedAPTextView.setText(outputText.toString());

        // set progress bar
        if (maxAp <= 0) maxAp = 1;
        _progressBar.setProgress(projectedAP * 100 / maxAp);
        int progressBarColor;
        if (projectedAP < desiredAp)
            progressBarColor = getColor(R.color.progressBarLow);
        else if (projectedAP < 0.9 * maxAp)
            progressBarColor = getColor(R.color.progressBarDesired);
        else if (projectedAP < maxAp)
            progressBarColor = getColor(R.color.progressBarNearFull);
        else
            progressBarColor = getColor(R.color.progressBarFull);
        _progressBar.setProgressTintList(
                ColorStateList.valueOf(progressBarColor));

        // schedule notifications
        if (notify)
        {
            Bitmap saber_stand = BitmapFactory.decodeResource(getResources(),
                    R.drawable.saber_stand);
            Bitmap saber_sad = BitmapFactory.decodeResource(getResources(),
                    R.drawable.saber_sad);
            NotificationCompat.Builder builder1 =
                    new NotificationCompat.Builder(this, "0")
                            .setSmallIcon(R.drawable.saber_notif)
                            .setLargeIcon(saber_stand)
                            .setContentTitle(getString(R.string.desiredNotifTitle))
                            .setContentText(getString(R.string.desiredNotifText))
                            .setWhen(desiredApTimeInMillis);
            NotificationPublisher.ScheduleNotificationInMinutes(this, 1,
                    builder1.build(), minTodesiredAp);
            NotificationCompat.Builder builder2 =
                    new NotificationCompat.Builder(this, "0")
                            .setSmallIcon(R.drawable.saber_notif)
                            .setLargeIcon(saber_sad)
                            .setContentTitle(getString(R.string.maxNotifTitle))
                            .setContentText(getString(R.string.maxNotifText))
                            .setWhen(maxApTimeInMillis);
            NotificationPublisher.ScheduleNotificationInMinutes(this, 2,
                    builder2.build(), minTomaxAp);
        }

        // save shared preferences
        SharedPreferences.Editor editor = _sharedPreferences.edit();
        editor.putLong(_SP_TIME_KEY, currTimeInMillis);
        editor.putInt(_SP_CURRENT_AP_KEY, currentAp);
        editor.putInt(_SP_DESIRED_AP_KEY, desiredAp);
        editor.putInt(_SP_MAX_AP_KEY, maxAp);
        editor.commit();
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
        UpdateState(Calendar.getInstance(), currentAp, desiredAp, maxAp, true);
    }

}
