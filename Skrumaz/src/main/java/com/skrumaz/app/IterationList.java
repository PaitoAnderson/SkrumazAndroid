package com.skrumaz.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.skrumaz.app.classes.Iteration;
import com.skrumaz.app.classes.Project;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.Store.Iterations;
import com.skrumaz.app.data.Store.Projects;
import com.skrumaz.app.data.WebService.GetIterations;
import com.skrumaz.app.data.WebService.GetProjects;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.DefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created by Paito Anderson on 11/18/2013.
 */
public class IterationList extends Activity implements OnRefreshListener, ActionBar.OnNavigationListener {

    private ListView listView;
    private LinearLayout processContainer;
    private TextView progressText;
    private ProgressBar progressSpinner;
    private IterationAdapter iterationAdapter;
    private ProjectAdapter projectAdapter;
    private Boolean continueRequests = true;
    private String breakingError = "";
    private Context mContext;
    private boolean noProjects = false;

    private List<Iteration> iterations = new ArrayList<Iteration>();
    private List<Project> dropdownValues;

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    private PullToRefreshLayout mPullToRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iteration_list);

        listView = (ListView) findViewById(R.id.listContainer);
        processContainer = (LinearLayout) findViewById(R.id.processContainer);
        progressText = (TextView) findViewById(R.id.progressText);
        progressSpinner = (ProgressBar) findViewById(R.id.progressSpinner);
        iterationAdapter = new IterationAdapter(this, iterations);
        listView.setAdapter(iterationAdapter);

        // Set Context variable to self
        mContext = this;

        // Set Projects from Store
        Projects db = new Projects(mContext);
        dropdownValues = db.getProjects();
        db.close();

        if (dropdownValues.isEmpty()) {
            noProjects = true;
            Project temp = new Project();
            temp.setName("Loading...");
            temp.setOid(Long.valueOf(0));
            dropdownValues.add(temp);
        }
        //Add spinner to select Workspace / Project from
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // Specify a SpinnerAdapter to populate the dropdown list.
        projectAdapter = new ProjectAdapter(actionBar.getThemedContext(), R.layout.spinner_item, R.id.projectName, dropdownValues);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(projectAdapter, this);

        updateActionbarSpinner();

        // Pull to Refresh Library - Initialize
        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(this)
                .allChildrenArePullable()
                .listener(this)
                .setup(mPullToRefreshLayout);

        // Pull to Refresh Library - Set ProgressBar color.
        DefaultHeaderTransformer transformer = ((DefaultHeaderTransformer)mPullToRefreshLayout.getHeaderTransformer());
        transformer.setProgressBarColor(getResources().getColor(R.color.accent_color));
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Initiate API Requests
        //populateListView(false);
    }

    @Override
    public void onRefreshStarted(View view) {
        populateListView(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.iteration, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        switch(item.getItemId()) {
            case R.id.action_refresh:
                // Initiate API Requests
                populateListView(true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {

        if (!dropdownValues.isEmpty()) {
            if (!dropdownValues.get(position).getOid().equals(0)) {
                Preferences.setProjectId(mContext, dropdownValues.get(position).getOid());
            }
        }

        populateListView(false);

        return true;
    }

    protected void populateListView(boolean forceRefresh) {
        Iterations db = new Iterations(getBaseContext());
        if (forceRefresh || db.isValidIterations(Preferences.getProjectId(getBaseContext(), true))) {
            new GetService().execute();
        } else {
            new GetStore().execute();
        }
        if (forceRefresh || noProjects) {
            new GetWorkspace().execute();
        }
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

            iterations.addAll(new GetIterations().FetchItems(mContext));

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
            if (iterations.isEmpty()) {
                progressSpinner.setVisibility(View.GONE);
                progressText.setText("No Iterations.");
            } else {
                processContainer.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
            }

            // Notify refresh finished
            mPullToRefreshLayout.setRefreshComplete();
            iterationAdapter.notifyDataSetChanged();

            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            // Pull Artifacts and Tasks from SQLite
            Iterations db = new Iterations(mContext);
            iterations.clear();
            iterations.addAll(db.getIterations(Preferences.getProjectId(getBaseContext(), true)));
            db.close();

            return null;
        }
    }

    class GetWorkspace extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPostExecute(Boolean result) {
            projectAdapter.notifyDataSetChanged();

            updateActionbarSpinner();

            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            // Replace list of items without breaking notifyDataSetChanged()
            List<Project> newList = new GetProjects().FetchItems(mContext);
            dropdownValues.clear();
            dropdownValues.addAll(newList);

            return null;
        }
    }

    public void startLoading() {
        // Reset Views / Spinner
        iterations.clear();
        processContainer.setVisibility(View.VISIBLE);
        progressSpinner.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
        progressText.setText("Getting Items..."); // Text updated using SetProgress()
        listView.setVisibility(View.GONE);
    }

    public void finishLoading() {
        if (continueRequests) {
            // If no items display empty state
            if (iterations.isEmpty()) {
                progressSpinner.setVisibility(View.GONE);
                progressText.setText("No Iterations.");
            } else {
                processContainer.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
            }
        }

        // Notify refresh finished
        mPullToRefreshLayout.setRefreshComplete();
        iterationAdapter.notifyDataSetChanged();
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

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current dropdown position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
                .getSelectedNavigationIndex());
        super.onSaveInstanceState(outState);
    }

    private void updateActionbarSpinner() {
        final ActionBar actionBar = getActionBar();
        Long currentProject = Preferences.getProjectId(mContext, true);
        if (currentProject > 0) {
            int currentPosition = Project.findOid(dropdownValues, currentProject);
            actionBar.setSelectedNavigationItem(currentPosition);
        }
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
