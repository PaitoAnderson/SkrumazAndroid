package com.skrumaz.app;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.skrumaz.app.classes.Artifact;
import com.skrumaz.app.classes.Iteration;
import com.skrumaz.app.classes.Task;
import com.skrumaz.app.data.Database;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.utils.StatusLookup;
import com.uservoice.uservoicesdk.Config;
import com.uservoice.uservoicesdk.UserVoice;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public class MainActivity extends Activity implements PullToRefreshAttacher.OnRefreshListener {

    private ExpandableListView listView;
    private LinearLayout processContainer;
    private TextView progressText;
    private ProgressBar progressSpinner;
    private ArtifactAdapter adapter;
    private Iteration iteration = new Iteration();
    private Boolean continueRequests = true;
    private String breakingError = "";

    private List<Artifact> artifacts = new ArrayList<Artifact>();

    private PullToRefreshAttacher mPullToRefreshAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ExpandableListView) findViewById(R.id.listContainer);
        processContainer = (LinearLayout) findViewById(R.id.processContainer);
        progressText = (TextView) findViewById(R.id.progressText);
        progressSpinner = (ProgressBar) findViewById(R.id.progressSpinner);
        adapter = new ArtifactAdapter(this, artifacts);
        listView.setAdapter(adapter);

        // Disable Group Indicator
        //listView.setGroupIndicator(null);

        // Pull to Refresh Library
        mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
        mPullToRefreshAttacher.addRefreshableView(listView, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!Preferences.isLoggedIn(getBaseContext())) {
            Intent login = new Intent(this, Login.class);
            startActivity(login);
            finish(); // Remove Activity from Stack
        } else {
            // Initiate API Requests
            populateExpandableListView(false);
        }
    }

    protected void populateExpandableListView(boolean forceRefesh) {

        // Reset Errors Flag/Message
        continueRequests = true;
        breakingError = "";

        if (forceRefesh || Preferences.isDataExpired(getBaseContext())) {
            // Get Current Iteration (This will trigger get User Stories and Defects after it completes)
            new GetIteration().execute();
        } else {

            new GetDatabase().execute();
        }

    }

    @Override
    public void onRefreshStarted(View view) {
        populateExpandableListView(true);
    }


    class GetIteration extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            // Reset Views / Spinner
            artifacts.clear();
            processContainer.setVisibility(View.VISIBLE);
            progressSpinner.setVisibility(View.VISIBLE);
            progressText.setVisibility(View.VISIBLE);
            progressText.setText("Getting Current Iteration...");
            listView.setVisibility(View.GONE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!continueRequests)
            {
                progressSpinner.setVisibility(View.GONE);
                progressText.setText(breakingError);
            } else {
                // Continue Process
                new GetUserStories().execute();
            }
            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            // Setup HTTP Request
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet("https://rally1.rallydev.com/slm/webservice/v2.0/iteration:current");

            Log.d("MainActivity", "https://rally1.rallydev.com/slm/webservice/v2.0/iteration:current");

            // Setup HTTP Headers / Authorization
            get.setHeader("Accept", "application/json");
            get.setHeader("Authorization", Preferences.getCredentials(getBaseContext()));
            try {
                // Make HTTP Request
                HttpResponse response = httpClient.execute(get);
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpURLConnection.HTTP_OK) {

                    // Parse JSON Response
                    BufferedReader streamReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    StringBuilder responseStrBuilder = new StringBuilder();
                    String inputStr;
                    while ((inputStr = streamReader.readLine()) != null) {
                        responseStrBuilder.append(inputStr);
                    }
                    JSONObject jsonIteration = new JSONObject(responseStrBuilder.toString());

                    // Get Iteration Name and Reference
                    iteration.setOid(jsonIteration.getJSONObject("Iteration").getString("ObjectID").toString());
                    iteration.setName(jsonIteration.getJSONObject("Iteration").getString("Name").toString());

                    Log.i("MainActivity", "Iteration: " + iteration.getName());
                } else {
                    continueRequests = false;
                    breakingError = statusLine.getReasonPhrase();
                    Log.d("MainActivity", "GI Error: " + statusLine.getReasonPhrase());
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    class GetUserStories extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            // Update Text
            progressText.setText("Getting User Stories...");
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!continueRequests)
            {
                progressSpinner.setVisibility(View.GONE);
                progressText.setText(breakingError);
            } else {
                // Continue Process
                new GetDefects().execute();
            }

            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            // Setup HTTP Request
            DefaultHttpClient httpClient = new DefaultHttpClient();
            String whereQuery = "((Iteration.Oid%20=%20%22" + iteration.getOid() + "%22)%20and%20(Owner.Name%20=%20%22" + Preferences.getUsername(getBaseContext()) + "%22))";

            if (Preferences.showAllOwners(getBaseContext())) {
                whereQuery = "(Iteration.Oid%20=%20%22" + iteration.getOid() + "%22)";
            }

            HttpGet get = new HttpGet("https://rally1.rallydev.com/slm/webservice/v2.0/hierarchicalrequirement?query=" + whereQuery + "&pagesize=100&fetch=Tasks:summary[FormattedID;Name;Blocked;State],Rank,Name,FormattedID,Blocked,ScheduleState&order=Rank");

                 Log.d("MainActivity","https://rally1.rallydev.com/slm/webservice/v2.0/hierarchicalrequirement?query=" + whereQuery + "&pagesize=100&fetch=Tasks:summary[FormattedID;Name;Blocked;State],Rank,Name,FormattedID,Blocked,ScheduleState&order=Rank");

            // Setup HTTP Headers / Authorization
            get.setHeader("Accept", "application/json");
            get.setHeader("Authorization", Preferences.getCredentials(getBaseContext()));
            try {
                // Make HTTP Request
                HttpResponse response = httpClient.execute(get);
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpURLConnection.HTTP_OK) {

                    // Parse JSON Response
                    BufferedReader streamReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    StringBuilder responseStrBuilder = new StringBuilder();
                    String inputStr;
                    while ((inputStr = streamReader.readLine()) != null) {
                        responseStrBuilder.append(inputStr);
                    }

                    // Get array of User Stories in Iteration for this user
                    JSONArray userStoriesArray = new JSONObject(responseStrBuilder.toString()).getJSONObject("QueryResult").getJSONArray("Results");

                    // Iterate though User Stories
                    for (int i = 0; i < userStoriesArray.length(); i++) {

                        // Create an expandable list item for each user story
                        Artifact userStory = new Artifact(userStoriesArray.getJSONObject(i).getString("Name"));
                        userStory.setRank(userStoriesArray.getJSONObject(i).getString("DragAndDropRank"));
                        userStory.setFormattedID(userStoriesArray.getJSONObject(i).getString("FormattedID"));
                        userStory.setBlocked(userStoriesArray.getJSONObject(i).getBoolean("Blocked"));
                        userStory.setStatus(StatusLookup.stringToStatus(userStoriesArray.getJSONObject(i).getString("ScheduleState")));

                        // Iterate though Tasks for User Story
                        for (int j = 0; j < userStoriesArray.getJSONObject(i).getJSONObject("Summary").getJSONObject("Tasks").getInt("Count"); j++)
                        {
                            Task task = new Task(userStoriesArray.getJSONObject(i).getJSONObject("Summary").getJSONObject("Tasks").getJSONObject("Name").names().getString(j));
                            task.setFormattedID(userStoriesArray.getJSONObject(i).getJSONObject("Summary").getJSONObject("Tasks").getJSONObject("FormattedID").names().getString(j));

                            // Add Tasks as children
                            userStory.addTask(task);
                        }

                        artifacts.add(userStory);
                    }

                } else {
                    continueRequests = false;
                    breakingError = statusLine.getReasonPhrase();
                    Log.d("MainActivity", "US Error: " + statusLine.getReasonPhrase());
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    class GetDefects extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            // Update Text
            progressText.setText("Getting Defects...");
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (continueRequests)
            {
                // If no items display empty state
                if (artifacts.isEmpty()) {
                    progressSpinner.setVisibility(View.GONE);
                    progressText.setText("No Items in Current Iteration.");
                } else {
                    processContainer.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);

                    // Sort by Rank
                    Collections.sort(artifacts, new Artifact.OrderByRank());
                }
            } else {
                progressSpinner.setVisibility(View.GONE);
                progressText.setText(breakingError);
            }

            // Notify refresh finished
            mPullToRefreshAttacher.setRefreshComplete();
            adapter.notifyDataSetChanged();

            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            // Setup HTTP Request
            DefaultHttpClient httpClient = new DefaultHttpClient();
            String whereQuery = "((Iteration.Oid%20=%20%22" + iteration.getOid() + "%22)%20and%20(Owner.Name%20=%20%22" + Preferences.getUsername(getBaseContext()) + "%22))";

            if (Preferences.showAllOwners(getBaseContext())) {
                whereQuery = "(Iteration.Oid%20=%20%22" + iteration.getOid() + "%22)";
            }

            HttpGet get = new HttpGet("https://rally1.rallydev.com/slm/webservice/v2.0/defects?query=" + whereQuery + "&pagesize=100&fetch=Tasks:summary[FormattedID;Name;Blocked;State],Rank,Name,FormattedID,Blocked,ScheduleState&order=Rank");

                 Log.d("MainActivity","https://rally1.rallydev.com/slm/webservice/v2.0/defects?query=" + whereQuery + "&pagesize=100&fetch=Tasks:summary[FormattedID;Name;Blocked;State],Rank,Name,FormattedID,Blocked,ScheduleState  &order=Rank");

            // Setup HTTP Headers / Authorization
            get.setHeader("Accept", "application/json");
            get.setHeader("Authorization", Preferences.getCredentials(getBaseContext()));
            try {
                // Make HTTP Request
                HttpResponse response = httpClient.execute(get);
                StatusLine statusLine = response.getStatusLine();
                if (statusLine.getStatusCode() == HttpURLConnection.HTTP_OK) {

                    // Parse JSON Response
                    BufferedReader streamReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                    StringBuilder responseStrBuilder = new StringBuilder();
                    String inputStr;
                    while ((inputStr = streamReader.readLine()) != null) {
                        responseStrBuilder.append(inputStr);
                    }

                    // Get array of User Stories in Iteration for this user
                    JSONArray defectsArray = new JSONObject(responseStrBuilder.toString()).getJSONObject("QueryResult").getJSONArray("Results");

                    // Iterate though Defects
                    for (int i = 0; i < defectsArray.length(); i++) {

                        // Create an expandable list item for each defect
                        Artifact defect = new Artifact(defectsArray.getJSONObject(i).getString("Name"));
                        defect.setRank(defectsArray.getJSONObject(i).getString("DragAndDropRank"));
                        defect.setFormattedID(defectsArray.getJSONObject(i).getString("FormattedID"));
                        defect.setBlocked(defectsArray.getJSONObject(i).getBoolean("Blocked"));
                        defect.setStatus(StatusLookup.stringToStatus(defectsArray.getJSONObject(i).getString("ScheduleState")));

                        // Iterate though Tasks for Defect
                        for (int j = 0; j < defectsArray.getJSONObject(i).getJSONObject("Summary").getJSONObject("Tasks").getInt("Count"); j++)
                        {
                            Task task = new Task(defectsArray.getJSONObject(i).getJSONObject("Summary").getJSONObject("Tasks").getJSONObject("Name").names().getString(j));
                            task.setFormattedID(defectsArray.getJSONObject(i).getJSONObject("Summary").getJSONObject("Tasks").getJSONObject("FormattedID").names().getString(j));

                            // Add Tasks as children
                            defect.addTask(task);
                        }

                        // Add defect with Tasks to List
                        artifacts.add(defect);
                    }

                    // Store artifacts in Database
                    Database db = new Database(getBaseContext());
                    db.storeArtifacts(artifacts);
                    db.close();

                    // Set data expiry date
                    Preferences.setDataExpiry(getBaseContext());

                } else {
                    continueRequests = false;
                    breakingError = statusLine.getReasonPhrase();
                    Log.d("MainActivity", "DE Error: " + statusLine.getReasonPhrase());
                }

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    class GetDatabase extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            // Reset Views / Spinner
            artifacts.clear();
            processContainer.setVisibility(View.VISIBLE);
            progressSpinner.setVisibility(View.VISIBLE);
            progressText.setVisibility(View.VISIBLE);
            progressText.setText("Getting Stuff...");
            listView.setVisibility(View.GONE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {

            // If no items display empty state
            if (artifacts.isEmpty()) {
                progressSpinner.setVisibility(View.GONE);
                progressText.setText("No Items in Current Iteration.");
            } else {
                processContainer.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);

                // Sort by Rank
                Collections.sort(artifacts, new Artifact.OrderByRank());
            }

            // Notify refresh finished
            mPullToRefreshAttacher.setRefreshComplete();
            adapter.notifyDataSetChanged();

            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            // Pull Artifacts and Task from SQLite
            Database db = new Database(getBaseContext());
            artifacts.clear();
            artifacts.addAll(db.getArtifacts());
            db.close();

            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        switch(item.getItemId()) {
            case R.id.action_refresh:
                // Initiate API Requests
                populateExpandableListView(true);
                break;
            case R.id.sort_rank:
                Collections.sort(artifacts, new Artifact.OrderByRank());
                adapter.notifyDataSetChanged();
                break;
            case R.id.sort_state:
                Collections.sort(artifacts, new Artifact.OrderByState());
                adapter.notifyDataSetChanged();
                break;
            case R.id.sort_id:
                Collections.sort(artifacts, new Artifact.OrderById());
                adapter.notifyDataSetChanged();
                break;
            case R.id.action_settings:
                // Launch Setting Activity
                Intent intent = new Intent(this, Settings.class);
                startActivity(intent);
                break;
            case R.id.action_help:
                // UserVoice library
                Config config = new Config("skrumaz.uservoice.com");
                UserVoice.init(config, this);

                // Launch UserVoice
                UserVoice.launchUserVoice(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
