package com.skrumaz.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.skrumaz.app.classes.Artifact;
import com.skrumaz.app.classes.Service;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.Store;
import com.skrumaz.app.data.WebServices;
import com.uservoice.uservoicesdk.Config;
import com.uservoice.uservoicesdk.UserVoice;

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
    private Boolean continueRequests = true;
    private String breakingError = "";
    private Service service;
    private Context mContext;

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

        // Set Context variable to self
        mContext = this;

        // Get Service Used
        service = Preferences.getService(mContext);

        // Disable Group Indicator
        listView.setGroupIndicator(null);

        // Pull to Refresh Library
        mPullToRefreshAttacher = PullToRefreshAttacher.get(this);
        mPullToRefreshAttacher.addRefreshableView(listView, this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!Preferences.isLoggedIn(mContext)) {
            Intent welcome = new Intent(this, Welcome.class);
            startActivity(welcome);
            finish(); // Remove Activity from Stack
        } else {
            // Initiate API Requests
            populateExpandableListView(false);
        }
    }

    protected void populateExpandableListView(boolean forceRefresh) {

        // Reset Errors Flag/Message
        continueRequests = true;
        breakingError = "";

        if (forceRefresh || Preferences.isDataExpired(mContext)) {
            new GetService().execute();
        } else {
            new GetStore().execute();
        }

    }

    @Override
    public void onRefreshStarted(View view) {
        populateExpandableListView(true);
    }

    class GetService extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {

            startLoading();

            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!continueRequests)
            {
                progressSpinner.setVisibility(View.GONE);
                progressText.setText(breakingError);
            }

            finishLoading();

            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            artifacts.addAll(WebServices.GetItems(service, mContext));

            return null;
        }
    }

    class GetStore extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {

            startLoading();

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

                // Sort Artifacts
                sortByDefault();
            }

            // Notify refresh finished
            mPullToRefreshAttacher.setRefreshComplete();
            adapter.notifyDataSetChanged();

            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            // Pull Artifacts and Tasks from SQLite
            Store db = new Store(mContext);
            artifacts.clear();
            artifacts.addAll(db.getArtifacts());
            db.close();

            return null;
        }
    }

    public void startLoading() {
        // Reset Views / Spinner
        artifacts.clear();
        processContainer.setVisibility(View.VISIBLE);
        progressSpinner.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
        progressText.setText("Getting Items..."); // Text updated using SetProgress()
        listView.setVisibility(View.GONE);
    }

    public void finishLoading() {
        if (continueRequests) {
            // If no items display empty state
            if (artifacts.isEmpty()) {
                progressSpinner.setVisibility(View.GONE);
                progressText.setText("No Items in Current Iteration.");
            } else {
                processContainer.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);

                // Sort Artifacts
                sortByDefault();
            }
        }

        // Notify refresh finished
        mPullToRefreshAttacher.setRefreshComplete();
        adapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

      //  Menu sortMenu = (Menu) menu.getItem(R.id.sort_menu);

        //sortMenu.add(0,R.id.sort_id,0,"ID");

        return super.onPrepareOptionsMenu(menu);
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
            case R.id.sort_modified:
                Collections.sort(artifacts, new Artifact.OrderByModified());
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

    // Sort based on preference
    public void sortByDefault() {
        String defaultSort = Preferences.getDefaultSort(mContext);

        if (defaultSort.equalsIgnoreCase("rank")) {
            Collections.sort(artifacts, new Artifact.OrderByRank());
        } else if (defaultSort.equalsIgnoreCase("state")) {
            Collections.sort(artifacts, new Artifact.OrderByState());
        } else if (defaultSort.equalsIgnoreCase("id")) {
            Collections.sort(artifacts, new Artifact.OrderById());
        } else if (defaultSort.equalsIgnoreCase("modified")) {
            Collections.sort(artifacts, new Artifact.OrderByModified());
        }
    }

    public void SetProgress(final String processMsg) {
        runOnUiThread(new Runnable() {
            public void run() {
                progressText.setText(processMsg);
            }
        });
    }

    public void SetError(final Boolean error, final String errorMsg) {
        this.continueRequests = error;
        this.breakingError = errorMsg;
    }
}
