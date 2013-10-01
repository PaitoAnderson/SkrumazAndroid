package com.skrumaz.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.skrumaz.app.data.Preferences;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.net.HttpURLConnection;

/**
 * Created by Paito Anderson on 2013-09-29.
 */
public class Login extends Activity {

    private ProgressDialog dialog;
    private EditText username;
    private EditText password;
    private Button login;
    private String errorInfo = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Find all elements on the page
        username = (EditText) findViewById(R.id.rallydev_username);
        password = (EditText) findViewById(R.id.rallydev_password);
        login = (Button) findViewById(R.id.login_button);

        // Fix textPasswords default font to match textEmailAddress
        password.setTypeface(Typeface.DEFAULT);
        password.setTransformationMethod(new PasswordTransformationMethod());

        // Notify handler for keyboard "Login" button clicks
        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    new GetUser().execute();
                    return true;
                } else {
                    return false;
                }
            }
        });

        // Notify handler for login button clicks
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetUser().execute();
            };
        });

    }

    class GetUser extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            // Present user with a loading spinner
            dialog = ProgressDialog.show(Login.this, "","Please Wait..." , true);
            dialog.show();

            super.onPreExecute();
        }
        @Override
        protected void onPostExecute(Boolean result) {
            // Remove loading spinner
            dialog.dismiss();

            // Inform the user of the error, likely Authentication or Network related
            if (errorInfo.length() > 0)
            {
                Toast.makeText(getBaseContext(), errorInfo, Toast.LENGTH_LONG).show();
                errorInfo = "";
            }

            super.onPostExecute(result);
        }
        @Override
        protected Boolean doInBackground(String... params) {
            // Setup HTTP Request
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet("https://rally1.rallydev.com/slm/webservice/v2.0/user");

            Log.d("Login", "https://rally1.rallydev.com/slm/webservice/v2.0/user");

            // Setup HTTP Headers / Authorization
            get.setHeader("Accept", "application/json");
            get.setHeader("Authorization", "Basic " + Base64.encodeToString((username.getText() + ":" + password.getText()).getBytes(), Base64.URL_SAFE | Base64.NO_WRAP));

            try {
                // Make HTTP Request
                HttpResponse response = httpClient.execute(get);
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpURLConnection.HTTP_OK) {
                    // Things are good!
                    Log.d("Login", "Things are good!");

                    // Store Credentials
                    Preferences.setCredentials(getBaseContext(), username.getText().toString(), password.getText().toString());

                    // Sent to MainActivity
                    Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(mainActivity);
                    finish(); // Remove Activity from Stack
                } else {
                    // Things are bad!
                    Log.d("Login", "Things are bad!");

                    // Inform the user of the issue, likely authentication.
                    errorInfo = statusLine.getReasonPhrase();
                }
            } catch (Exception e) {

                // Inform the user of the issue, likely network related.
                errorInfo = e.getMessage();
                e.printStackTrace();
            }

            return null;
        }
    }

}
