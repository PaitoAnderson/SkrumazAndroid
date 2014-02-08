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
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.skrumaz.app.classes.Artifact;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.Store.Artifacts;
import com.skrumaz.app.data.WebService.GetArtifacts;
import com.uservoice.uservoicesdk.Config;
import com.uservoice.uservoicesdk.UserVoice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.DefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class ArtifactList extends Activity implements OnRefreshListener {

    private ExpandableListView listView;
    private LinearLayout processContainer;
    private TextView progressText;
    private ProgressBar progressSpinner;
    private ArtifactAdapter adapter;
    private Boolean continueRequests = true;
    private String breakingError = "";
    private Context mContext;

    private List<Artifact> artifacts = new ArrayList<Artifact>();

    private PullToRefreshLayout mPullToRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artifact_list);

        listView = (ExpandableListView) findViewById(R.id.listContainer);
        processContainer = (LinearLayout) findViewById(R.id.processContainer);
        progressText = (TextView) findViewById(R.id.progressText);
        progressSpinner = (ProgressBar) findViewById(R.id.progressSpinner);
        adapter = new ArtifactAdapter(this, artifacts);
        listView.setAdapter(adapter);

        // Set Context variable to self
        mContext = this;

        // Disable Group Indicator
        listView.setGroupIndicator(null);

        // Pull to Refresh Library - Initialize
        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(this)
                .allChildrenArePullable()
                .listener(this)
                .setup(mPullToRefreshLayout);

        // Add back button icon
        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Pull to Refresh Library - Set ProgressBar color.
        DefaultHeaderTransformer transformer = ((DefaultHeaderTransformer)mPullToRefreshLayout.getHeaderTransformer());
        transformer.setProgressBarColor(getResources().getColor(R.color.accent_color));
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

        Artifacts db = new Artifacts(getBaseContext());
        if (forceRefresh || db.isValidArtifacts(Preferences.getIterationId(getBaseContext(), true))) {
            new GetService().execute();
        } else {
            new GetStore().execute();
        }
    }

    @Override
    public void onRefreshStarted(View view) {
        populateExpandableListView(true);
    }

    private class GetService extends AsyncTask<String, Integer, Boolean> {

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

            artifacts.addAll(new GetArtifacts().FetchItems(mContext));

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
            mPullToRefreshLayout.setRefreshComplete();
            adapter.notifyDataSetChanged();

            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            // Pull Artifacts and Tasks from SQLite
            Artifacts db = new Artifacts(mContext);
            artifacts.clear();
            artifacts.addAll(db.getArtifacts(Preferences.getIterationId(getBaseContext(), true)));
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
        mPullToRefreshLayout.setRefreshComplete();
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.artifact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        switch(item.getItemId()) {
            case android.R.id.home:
                // Sent to Iteration List
                startActivity(new Intent(getApplicationContext(), IterationList.class));
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out);
                break;
            case R.id.create_us:
                // Create User Story
                Intent createUs = new Intent(this, Create.class);
                createUs.putExtra("CreateName", mContext.getResources().getString(R.string.action_create_us));
                createUs.putExtra("CreateType", "HierarchicalRequirement");
                startActivity(createUs);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                break;
            case R.id.create_de:
                // Create Defect
                Intent createDe = new Intent(this, Create.class);
                createDe.putExtra("CreateName", mContext.getResources().getString(R.string.action_create_de));
                createDe.putExtra("CreateType", "Defect");
                startActivity(createDe);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                break;
            case R.id.create_ds:
                // Create Defect Suite
                Intent createDs = new Intent(this, Create.class);
                createDs.putExtra("CreateName", mContext.getResources().getString(R.string.action_create_ds));
                createDs.putExtra("CreateType", "DefectSuite");
                startActivity(createDs);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                break;
            case R.id.create_ts:
                // Create Defect Suite
                Intent createTs = new Intent(this, Create.class);
                createTs.putExtra("CreateName", mContext.getResources().getString(R.string.action_create_ts));
                createTs.putExtra("CreateType", "TestSet");
                startActivity(createTs);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
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
            case R.id.sort_name:
                Collections.sort(artifacts, new Artifact.OrderByName());
                adapter.notifyDataSetChanged();
                break;
            case R.id.sort_modified:
                Collections.sort(artifacts, new Artifact.OrderByModified());
                adapter.notifyDataSetChanged();
                break;
            case R.id.action_refresh:
                // Initiate API Requests
                Toast.makeText(mContext, mContext.getResources().getString(R.string.tip_swipe_down), Toast.LENGTH_LONG).show();
                populateExpandableListView(true);
                break;
            case R.id.action_settings:
                // Launch Setting Activity
                startActivity(new Intent(this, Settings.class));
                break;
            case R.id.action_help:
                // UserVoice library
                UserVoice.init(new Config("skrumaz.uservoice.com"), this);

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
        } else if (defaultSort.equalsIgnoreCase("name")) {
            Collections.sort(artifacts, new Artifact.OrderByName());
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

    public void SetError(final String errorMsg) {
        this.continueRequests = false;
        this.breakingError = errorMsg;
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }
}
