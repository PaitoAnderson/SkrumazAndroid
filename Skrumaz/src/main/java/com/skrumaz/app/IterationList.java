package com.skrumaz.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.skrumaz.app.classes.Iteration;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.Store.Iterations;
import com.skrumaz.app.data.WebService.GetIterations;

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
    private IterationAdapter adapter;
    private Boolean continueRequests = true;
    private String breakingError = "";
    private Context mContext;

    private List<Iteration> iterations = new ArrayList<Iteration>();

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
        adapter = new IterationAdapter(this, iterations);
        listView.setAdapter(adapter);

        // Set Context variable to self
        mContext = this;

        //Add spinner to select Workspace / Project from
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        final String[] dropdownValues = { "Project 1", "Project 2", "Project 3"};

        // Specify a SpinnerAdapter to populate the dropdown list.
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(actionBar.getThemedContext(),
                R.layout.spinner_item, android.R.id.text1,
                dropdownValues);

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(adapter, this);

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
        populateListView(false);
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


        return true;
    }

    protected void populateListView(boolean forceRefresh) {
        Iterations db = new Iterations(getBaseContext());
        if (forceRefresh || db.isValidIterations(Preferences.getProjectId(getBaseContext(), true))) {
            new GetService().execute();
        } else {
            new GetStore().execute();
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
            adapter.notifyDataSetChanged();

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
        adapter.notifyDataSetChanged();
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
    }
}
