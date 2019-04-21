package dev.chuahou.fgo_ap_reminder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("Fate/AP calculator");
    }

    public void buttonClicked(View view) {
        // get text view to output results to
        TextView outputTextView = (TextView) findViewById(R.id.outputTextView);
    }

}
