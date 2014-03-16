package com.skrumaz.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.skrumaz.app.classes.Project;
import com.skrumaz.app.classes.Workspace;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.Store.Projects;
import com.skrumaz.app.data.Store.Workspaces;
import com.skrumaz.app.data.WebService.GetProjects;
import com.skrumaz.app.data.WebService.GetWorkspaces;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.DefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created by Paito Anderson on 2014-03-01.
 */
public class ProjectList extends Activity implements OnRefreshListener, ActionBar.OnNavigationListener {

    private ListView listView;
    private LinearLayout processContainer;
    private TextView progressText;
    private ProgressBar progressSpinner;
    private ProjectAdapter projectAdapter;
    private WorkspaceAdapter workspaceAdapter;
    private Boolean continueRequests = true;
    private String breakingError = "";
    private Context mContext;
    private boolean noWorkspaces = false;
    private boolean syntheticSelection = true;

    private List<Project> projects = new ArrayList<Project>();
    private List<Workspace> dropdownValues;

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    private PullToRefreshLayout mPullToRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        listView = (ListView) findViewById(R.id.listContainer);
        processContainer = (LinearLayout) findViewById(R.id.processContainer);
        progressText = (TextView) findViewById(R.id.progressText);
        progressSpinner = (ProgressBar) findViewById(R.id.progressSpinner);
        projectAdapter = new ProjectAdapter(this, projects);
        listView.setAdapter(projectAdapter);

        // Set Context variable to self
        mContext = this;

        // Set Projects from Store
        Workspaces db = new Workspaces(mContext);
        dropdownValues = db.getWorkspaces();
        db.close();

        if (dropdownValues.isEmpty()) {
            noWorkspaces = true;
            Workspace temp = new Workspace();
            temp.setName("Loading...");
            temp.setOid((long) 0);
            dropdownValues.add(temp);
        }
        //Add spinner to select Workspace / Project from
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // Specify a SpinnerAdapter to populate the dropdown list.
        workspaceAdapter = new WorkspaceAdapter(actionBar.getThemedContext(), R.layout.spinner_item, R.id.workspaceName, dropdownValues);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(workspaceAdapter, this);

        updateActionbarSpinner();

        // Pull to Refresh Library - Initialize
        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(this)
                .allChildrenArePullable()
                .listener(this)
                .setup(mPullToRefreshLayout);

        // Set activity title
        getActionBar().setTitle(getResources().getString(R.string.action_projects));

        // Pull to Refresh Library - Set ProgressBar color.
        DefaultHeaderTransformer transformer = ((DefaultHeaderTransformer)mPullToRefreshLayout.getHeaderTransformer());
        transformer.setProgressBarColor(getResources().getColor(R.color.accent_color));
    }

    @Override
    public void onRefreshStarted(View view) {
        // Initiate API Requests
        populateListView(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.project, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        switch(item.getItemId()) {
            case R.id.action_refresh:
                // Initiate API Requests
                Toast.makeText(mContext, mContext.getResources().getString(R.string.tip_swipe_down), Toast.LENGTH_LONG).show();
                populateListView(true);
                break;
            case R.id.create_project:
                // Create Project
                Intent createProject = new Intent(this, Create.class);
                createProject.putExtra("CreateName", mContext.getResources().getString(R.string.action_project));
                createProject.putExtra("CreateType", "Project");
                startActivity(createProject);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                break;
            case R.id.create_workspace:
                // Create Workspace
                Intent createWorkspace = new Intent(this, Create.class);
                createWorkspace.putExtra("CreateName", mContext.getResources().getString(R.string.action_workspace));
                createWorkspace.putExtra("CreateType", "Workspace");
                startActivity(createWorkspace);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Initiate API Requests
        populateListView(false);
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id) {

        if (syntheticSelection) {
            syntheticSelection = false;
            return true;
        }

        if (!dropdownValues.isEmpty()) {
            if (dropdownValues.get(position).getOid() != 0) {
                Preferences.setWorkspaceId(mContext, dropdownValues.get(position).getOid());
            }
        }

        populateListView(false);

        return true;
    }

    protected void populateListView(boolean forceRefresh) {
        Projects db = new Projects(getBaseContext());
        if (forceRefresh || db.isValidProjects(Preferences.getWorkspaceId(getBaseContext(), true))) {
            new GetService().execute();
        } else {
            new GetStore().execute();
        }
        if (forceRefresh || noWorkspaces) {
            new GetWorkspace().execute();
        }
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

            projects.addAll(new GetProjects().FetchItems(mContext));

            return null;
        }
    }

    private class GetStore extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {

            startLoading();

            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {

            // If no items display empty state
            if (projects.isEmpty()) {
                progressSpinner.setVisibility(View.GONE);
                progressText.setText("No Projects.");
            } else {
                processContainer.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
            }

            // Notify refresh finished
            mPullToRefreshLayout.setRefreshComplete();
            projectAdapter.notifyDataSetChanged();

            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            // Pull Artifacts and Tasks from SQLite
            Projects db = new Projects(mContext);
            projects.clear();
            projects.addAll(db.getProjects(Preferences.getWorkspaceId(getBaseContext(), true)));
            db.close();

            return null;
        }
    }

    private class GetWorkspace extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPostExecute(Boolean result) {
            workspaceAdapter.notifyDataSetChanged();

            updateActionbarSpinner();

            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            // Replace list of items without breaking notifyDataSetChanged()
            List<Workspace> newList = new GetWorkspaces().FetchItems(mContext);
            dropdownValues.clear();
            dropdownValues.addAll(newList);

            return null;
        }
    }

    public void startLoading() {
        // Reset Views / Spinner
        projects.clear();
        processContainer.setVisibility(View.VISIBLE);
        progressSpinner.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
        progressText.setText("Getting Items..."); // Text updated using SetProgress()
        listView.setVisibility(View.GONE);
    }

    public void finishLoading() {
        if (continueRequests) {
            // If no items display empty state
            if (projects.isEmpty()) {
                progressSpinner.setVisibility(View.GONE);
                progressText.setText("No Projects.");
            } else {
                processContainer.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
            }
        }

        // Notify refresh finished
        mPullToRefreshLayout.setRefreshComplete();
        projectAdapter.notifyDataSetChanged();
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
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar().getSelectedNavigationIndex());
        super.onSaveInstanceState(outState);
    }

    private void updateActionbarSpinner() {
        final ActionBar actionBar = getActionBar();
        Long currentWorkspace = Preferences.getWorkspaceId(mContext, true);
        if (currentWorkspace > 0) {
            int currentPosition = Workspace.findOid(dropdownValues, currentWorkspace);
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
