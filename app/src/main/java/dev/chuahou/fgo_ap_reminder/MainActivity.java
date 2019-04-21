package dev.chuahou.fgo_ap_reminder;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

        getSupportActionBar().setTitle("Fate/AP calculator");

        // create notification channel
        CharSequence name = "Notification Channel";
        String description = "Notification Channel";
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
            outputTextView.setText("ちゃんと入力してくださいよ、マスター。");
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
        outputText.append("現在時刻：" + df.format(c.getTime()) + "\n");

        // get time to desired AP
        c.add(Calendar.MINUTE, minToDesiredAP);
        outputText.append("欲しいAP数まで：" + minToDesiredAP + "分\n");
        outputText.append("欲しいAP数時刻：" + df.format(c.getTime()) + "\n");

        // get time to max AP
        c.add(Calendar.MINUTE, minToMaxAP - minToDesiredAP);
        outputText.append("APが増えすぎるまで：" + minToMaxAP + "分\n");
        outputText.append("APが増えすぎる時刻：" + df.format(c.getTime()) + "\n");

        // set text view text
        outputTextView.setText(outputText.toString());

        // schedule notifications
        NotificationCompat.Builder builder1 = new NotificationCompat.Builder(this, "0")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("行きましょう、マスター。")
                .setContentText("APが足りました。決着をつけましょう。");
        scheduleNotification(1, builder1.build(), minToDesiredAP);
        NotificationCompat.Builder builder2 = new NotificationCompat.Builder(this, "0")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("まだですか？マスター。")
                .setContentText("そろそろ行った方がいいではないですか？");
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
