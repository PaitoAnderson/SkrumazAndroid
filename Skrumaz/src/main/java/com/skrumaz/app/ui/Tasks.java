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
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.skrumaz.app.ArtifactAdapter;
import com.skrumaz.app.R;
import com.skrumaz.app.classes.Artifact;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.Store.Artifacts;
import com.skrumaz.app.data.WebService.GetArtifacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.DefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created by Paito Anderson on 2014-03-16.
 */
public class Tasks extends Fragment implements OnRefreshListener {

    private ExpandableListView listView;
    private LinearLayout processContainer;
    private ProgressBar progressSpinner;
    private ArtifactAdapter adapter;
    private Context mContext;
    private List<Artifact> artifacts = new ArrayList<Artifact>();
    private PullToRefreshLayout mPullToRefreshLayout;

    public TextView progressText;
    public Boolean continueRequests = true;
    public String breakingError = "";

    public Tasks() {
        // Empty constructor required for fragment subclasses
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Set Title
        getActivity().setTitle("Tasks");

        // Get Context
        mContext = getActivity();

        // Inflate View
        View tasksView = inflater.inflate(R.layout.activity_artifact_list, container, false);

        // Find things in the View
        listView = (ExpandableListView) tasksView.findViewById(R.id.listContainer);
        processContainer = (LinearLayout) tasksView.findViewById(R.id.processContainer);
        progressText = (TextView) tasksView.findViewById(R.id.progressText);
        progressSpinner = (ProgressBar) tasksView.findViewById(R.id.progressSpinner);
        adapter = new ArtifactAdapter(getActivity(), artifacts);
        listView.setAdapter(adapter);

        // Remove spinner select Workspace / Project from
        final ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        // Disable Group Indicator
        listView.setGroupIndicator(null);

        // Pull to Refresh Library - Initialize
        mPullToRefreshLayout = (PullToRefreshLayout) tasksView.findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(getActivity())
                .allChildrenArePullable()
                .listener(this)
                .setup(mPullToRefreshLayout);

        // Pull to Refresh Library - Set ProgressBar color.
        DefaultHeaderTransformer transformer = ((DefaultHeaderTransformer)mPullToRefreshLayout.getHeaderTransformer());
        transformer.setProgressBarColor(getResources().getColor(R.color.accent_color));

        // Notify System we have our own menu items
        setHasOptionsMenu(true);

        // Return View
        return tasksView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initiate API Requests
        populateExpandableListView(false);
    }

    @Override
    public void onRefreshStarted(View view) {
        populateExpandableListView(true);
    }

    public void populateExpandableListView(boolean forceRefresh) {

        // Reset Errors Flag/Message
        continueRequests = true;
        breakingError = "";

        Artifacts db = new Artifacts(mContext);
        if (forceRefresh || db.isValidArtifacts(Preferences.getIterationId(mContext, true))) {
            new GetService().execute();
        } else {
            new GetStore().execute();
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
            artifacts.addAll(db.getArtifacts(Preferences.getIterationId(mContext, true)));
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.artifact, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        switch (item.getItemId()) {
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

    public void SetError2(final String errorMsg) {
        continueRequests = false;
        breakingError = errorMsg;
    }
}
