package com.skrumaz.app.ui;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.skrumaz.app.ProjectAdapter;
import com.skrumaz.app.R;
import com.skrumaz.app.WorkspaceAdapter;
import com.skrumaz.app.classes.Project;
import com.skrumaz.app.classes.Workspace;
import com.skrumaz.app.data.Preferences;
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
 * Created by Paito Anderson on 2014-03-16.
 */
public class Projects extends Fragment implements OnRefreshListener, ActionBar.OnNavigationListener {

    private ListView listView;
    private LinearLayout processContainer;
    private ProgressBar progressSpinner;
    private ProjectAdapter projectAdapter;
    private WorkspaceAdapter workspaceAdapter;
    private boolean noWorkspaces = false;
    private boolean syntheticSelection = true;
    private Context mContext;
    private List<Project> projects = new ArrayList<Project>();
    private List<Workspace> dropdownValues;
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
    private PullToRefreshLayout mPullToRefreshLayout;
    private Tracker mTracker;

    public TextView progressText;
    public Boolean continueRequests = true;
    public String breakingError = "";

    public Projects() {
        // Empty constructor required for fragment subclasses
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Set Title
        getActivity().setTitle("Projects");

        // Get Context
        mContext = getActivity();

        // Inflate View
        View projectsView = inflater.inflate(R.layout.activity_project_list, container, false);

        // Find things in the View
        listView = (ListView) projectsView.findViewById(R.id.listContainer);
        processContainer = (LinearLayout) projectsView.findViewById(R.id.processContainer);
        progressText = (TextView) projectsView.findViewById(R.id.progressText);
        progressSpinner = (ProgressBar) projectsView.findViewById(R.id.progressSpinner);
        projectAdapter = new ProjectAdapter(getActivity(), projects);
        listView.setAdapter(projectAdapter);

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
        final ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // Specify a SpinnerAdapter to populate the dropdown list.
        workspaceAdapter = new WorkspaceAdapter(actionBar.getThemedContext(), R.layout.spinner_item, R.id.workspaceName, dropdownValues);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(workspaceAdapter, this);

        updateActionbarSpinner();

        // Pull to Refresh Library - Initialize
        mPullToRefreshLayout = (PullToRefreshLayout) projectsView.findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(getActivity())
                .allChildrenArePullable()
                .listener(this)
                .setup(mPullToRefreshLayout);

        // Notify System we have our own menu items
        setHasOptionsMenu(true);

        // Pull to Refresh Library - Set ProgressBar color.
        DefaultHeaderTransformer transformer = ((DefaultHeaderTransformer)mPullToRefreshLayout.getHeaderTransformer());
        transformer.setProgressBarColor(getResources().getColor(R.color.accent_color));

        // Return View
        return projectsView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initiate API Requests
        populateListView(false);

        // Google Analytics
        mTracker = EasyTracker.getInstance(getActivity());
    }

    @Override
    public void onRefreshStarted(View view) {
        populateListView(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.project, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        switch (item.getItemId()) {
            case R.id.action_refresh:
                // Initiate API Requests
                Toast.makeText(mContext, mContext.getResources().getString(R.string.tip_swipe_down), Toast.LENGTH_LONG).show();
                populateListView(true);
                break;
        }
        return super.onOptionsItemSelected(item);
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
        com.skrumaz.app.data.Store.Projects db = new com.skrumaz.app.data.Store.Projects(mContext);
        if (forceRefresh || db.isValidProjects(Preferences.getWorkspaceId(mContext, true))) {
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
            com.skrumaz.app.data.Store.Projects db = new com.skrumaz.app.data.Store.Projects(mContext);
            projects.clear();
            projects.addAll(db.getProjects(Preferences.getWorkspaceId(mContext, true)));
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

    /*@Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActivity().getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Serialize the current dropdown position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActivity().getActionBar().getSelectedNavigationIndex());
        super.onSaveInstanceState(outState);
    }*/

    private void updateActionbarSpinner() {
        final ActionBar actionBar = getActivity().getActionBar();
        Long currentWorkspace = Preferences.getWorkspaceId(mContext, true);
        if (currentWorkspace > 0) {
            int currentPosition = Workspace.findOid(dropdownValues, currentWorkspace);
            if (actionBar.getNavigationMode() == ActionBar.NAVIGATION_MODE_LIST) {
                actionBar.setSelectedNavigationItem(currentPosition);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mTracker.set(Fields.SCREEN_NAME, "Projects");
        mTracker.send(MapBuilder.createAppView().build());
    }
}