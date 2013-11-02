package com.skrumaz.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.skrumaz.app.classes.Service;
import com.skrumaz.app.data.Preferences;

/**
 * Created by Paito Anderson on 10/16/2013.
 */
public class Welcome extends Activity {

    private Button rallySoftware;
    private Button pivotalLabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Find all elements on the page
        rallySoftware = (Button) findViewById(R.id.rally_software);
        pivotalLabs = (Button) findViewById(R.id.pivotal_labs);

        // Notify handler for Rally Dev button clicks
        rallySoftware.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetService(Service.RALLY_DEV);
            };
        });

        // Notify handler for Pivotal Labs button clicks
        pivotalLabs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SetService(Service.PIVOTAL_TRACKER);
            };
        });
    }

    protected void SetService(Service service) {
        // Set Service Used
        Preferences.setService(getBaseContext(), service);

        // Sent to Login
        startActivity(new Intent(getApplicationContext(), Login.class));
        overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
        finish(); // Remove Activity from Stack
    }
}