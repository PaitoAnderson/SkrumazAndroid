package com.skrumaz.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by Paito Anderson on 15-01-01.
 */
public class RedirectIntent extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handle(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handle(intent);
    }

    private void handle(Intent intent) {

        // Send to MainActivity
        Intent home = new Intent(this, MainActivity.class);
        startActivity(home);
        finish();

        // TODO: Parse URL and send to correct view.
        final Uri action = intent.getData();
        Log.d("URL: ", "RallyDev URL: " + action.toString());
    }
}
