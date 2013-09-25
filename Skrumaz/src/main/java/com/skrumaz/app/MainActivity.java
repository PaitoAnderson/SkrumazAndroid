package com.skrumaz.app;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import com.skrumaz.app.data.Preferences;
import com.uservoice.uservoicesdk.Config;
import com.uservoice.uservoicesdk.UserVoice;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

public class MainActivity extends Activity implements PullToRefreshAttacher.OnRefreshListener {

    ExpandableListView listView;
    LinearLayout processContainer;
    TextView progressText;
    ProgressBar progressSpinner;
    SparseArray<Group> userStories = new SparseArray<Group>();
    UserStoryAdapter adapter;
    String iterationID;
    String iterationName;
    boolean continueRequests = true;
    String breakingError = "";

    private PullToRefreshAttacher mPullToRefreshAttacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ExpandableListView) findViewById(R.id.listContainer);
        processContainer = (LinearLayout) findViewById(R.id.processContainer);
        progressText = (TextView) findViewById(R.id.progressText);
        progressSpinner = (ProgressBar) findViewById(R.id.progressSpinner);
        adapter = new UserStoryAdapter(this, userStories);
        listView.setAdapter(adapter);

        //Disable Group Indicator
        //listView.setGroupIndicator(null);

        // UserVoice Library
        Config config = new Config("skrumaz.uservoice.com");
        UserVoice.init(config, this);

        // Pull to Refresh Library
        mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
        mPullToRefreshAttacher.addRefreshableView(listView, this);

        // Initiate API Requests
        populateExpandableListView();
    }

    protected void populateExpandableListView() {

        // Get Current Iteration URL
        new GetIteration().execute();


        if (continueRequests) {
            // We have added data too userStories
            adapter.notifyDataSetChanged();
        }

        // Reset Errors Flag/Message
        continueRequests = true;
        breakingError = "";
    }

    @Override
    public void onRefreshStarted(View view) {
        populateExpandableListView();
    }

    class GetIteration extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            // Reset Views / Spinner
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
                //Continue Process
                new GetUserStories().execute();
            }
            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            // Setup HTTP Request
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet("https://rally1.rallydev.com/slm/webservice/v2.0/iteration:current");

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
                    JSONObject iteration = new JSONObject(responseStrBuilder.toString());

                    // Get Iteration Name and Reference
                    iterationID = iteration.getJSONObject("Iteration").getString("ObjectID").toString();
                    iterationName = iteration.getJSONObject("Iteration").getString("Name").toString();

                    Log.i("MainActivity", "Iteration: " + iterationName);
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
            if (continueRequests)
            {
                processContainer.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
            } else {
                progressSpinner.setVisibility(View.GONE);
                progressText.setText(breakingError);
            }

            //Notify refresh finished
            mPullToRefreshAttacher.setRefreshComplete();

            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            // Sample Data
            //for (int j = 0; j < 5; j++) {
            //    Group group = new Group("Testing with updated User Interface Phase " + j);
            //    for (int i = 0; i < 5; i++) {
            //        group.children.add("Testing with updated User Interface Phase " + i);
            //    }
            //    userStories.append(j, group);
            //}

            // Setup HTTP Request
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet get = new HttpGet("https://rally1.rallydev.com/slm/webservice/v2.0/hierarchicalrequirement?query=((Iteration.Oid%20=%20%22" + iterationID + "%22)%20and%20(Owner.Name%20=%20%22" + Preferences.getUsername(getBaseContext()) + "%22))&fetch=Tasks:summary[FormattedID;Name],Rank&order=Rank");

                 Log.d("MainActivity","https://rally1.rallydev.com/slm/webservice/v2.0/hierarchicalrequirement?query=((Iteration.Oid%20=%20%22" + iterationID + "%22)%20and%20(Owner.Name%20=%20%22" + Preferences.getUsername(getBaseContext()) + "%22))&fetch=Tasks:summary[FormattedID;Name],Rank&order=Rank");

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
                        Group userStory = new Group(userStoriesArray.getJSONObject(i).getString("_refObjectName").toString());

                        // Iterate though Tasks for User Story
                        for (int j = 0; j < userStoriesArray.getJSONObject(i).getJSONObject("Summary").getJSONObject("Tasks").getInt("Count"); j++)
                        {
                            // Add Tasks as children
                            userStory.children.add(userStoriesArray.getJSONObject(i).getJSONObject("Summary").getJSONObject("Tasks").getJSONObject("Name").names().getString(j));
                        }

                        userStories.append(i, userStory);
                    }



                    //Log.i("MainActivity", "Iteration: " + iterationName);

                } else {
                    continueRequests = false;
                    breakingError = statusLine.getReasonPhrase();
                    Log.d("MainActivity", "US Error: " + statusLine.getReasonPhrase());
                }

                // Items for Iteration
                //(Iteration = "https://rally1.rallydev.com/slm/webservice/v2.0/iteration/14094993021")
                //https://rally1.rallydev.com/slm/webservice/v2.0/hierarchicalrequirement?query=(Iteration%20=%20%22https://rally1.rallydev.com/slm/webservice/v2.0/iteration/14094993021%22)
                //https://rally1.rallydev.com/slm/webservice/v2.0/defect?query=(Iteration%20=%20%22https://rally1.rallydev.com/slm/webservice/v2.0/iteration/14094993021%22)


                //https://rally1.rallydev.com/slm/webservice/v2.0/artifact?query=(Iteration.Oid%20%=%20%2214094993021%22)&start=1&pagesize=20
                //https://rally1.rallydev.com/slm/webservice/v2.0/artifact?query=((Iteration.Oid%20%=%20"14094993021")%20and%20(Owner.Name%20=%20%22panderso@qualcomm.com%22))&start=1&pagesize=20


                //All stories with Tasks for current Owner / Iteration
                //https://rally1.rallydev.com/slm/webservice/v2.0/task?workspace=https://rally1.rallydev.com/slm/webservice/v2.0/workspace/3418690630&query=((Iteration.Oid%20%3D%2014094993021)%20and%20(Owner.Name%20%3D%20panderso%40qualcomm.com))&fetch=true&order=WorkProduct&start=1&pagesize=20

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

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
                populateExpandableListView();
                break;
            case R.id.action_settings:
                Intent intent = new Intent(this, Settings.class);
                startActivity(intent);
                break;
            case R.id.action_help:
                UserVoice.launchUserVoice(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
}
