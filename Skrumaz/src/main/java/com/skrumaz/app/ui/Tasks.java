package com.skrumaz.app.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.skrumaz.app.R;
import com.skrumaz.app.classes.Artifact;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.Store.Artifacts;
import com.skrumaz.app.data.WebService.GetArtifacts;
import com.skrumaz.app.ui.adapters.ArtifactAdapter;
import com.skrumaz.app.utils.ArtifactSort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Paito Anderson on 2014-03-16.
 */
public class Tasks extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private Context mContext;
    private List<Artifact> artifactCards = new ArrayList<Artifact>();

    private RecyclerView recyclerView;
    private RecyclerView.Adapter recyclerViewAdapter;

    private LinearLayout processContainer;
    private ProgressBar progressSpinner;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private Tracker mTracker;

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
        recyclerView = (RecyclerView) tasksView.findViewById(R.id.artifactList);
        processContainer = (LinearLayout) tasksView.findViewById(R.id.processContainer);
        progressText = (TextView) tasksView.findViewById(R.id.progressText);
        progressSpinner = (ProgressBar) tasksView.findViewById(R.id.progressSpinner);
        recyclerViewAdapter = new ArtifactAdapter(getActivity().getBaseContext(), artifactCards);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Swipe to Refresh Library - Initialize
        mSwipeRefreshLayout = (SwipeRefreshLayout) tasksView.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent_color);

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

        // Google Analytics
        mTracker = EasyTracker.getInstance(getActivity());
    }

    @Override
    public void onRefresh() {
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

            artifactCards.addAll(new GetArtifacts().FetchItems(mContext));

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
            if (artifactCards.isEmpty()) {
                progressSpinner.setVisibility(View.GONE);
                progressText.setText("No Items in Current Iteration.");
            } else {
                processContainer.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                // Sort Artifacts
                sortByDefault();
            }

            // Notify refresh finished
            mSwipeRefreshLayout.setRefreshing(false);
            recyclerViewAdapter.notifyDataSetChanged();

            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            // Pull Artifacts and Tasks from SQLite
            Artifacts db = new Artifacts(mContext);
            artifactCards.clear();
            artifactCards.addAll(db.getArtifacts(Preferences.getIterationId(mContext, true)));
            db.close();

            return null;
        }
    }

    public void startLoading() {
        // Reset Views / Spinner
        artifactCards.clear();
        processContainer.setVisibility(View.VISIBLE);
        progressSpinner.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
        progressText.setText("Getting Items..."); // Text updated using SetProgress()
        recyclerView.setVisibility(View.GONE);
    }

    public void finishLoading() {
        if (continueRequests) {
            // If no items display empty state
            if (artifactCards.isEmpty()) {
                progressSpinner.setVisibility(View.GONE);
                progressText.setText("No Items in Current Iteration.");
            } else {
                processContainer.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                // Sort Artifacts
                sortByDefault();
            }
        }

        // Notify refresh finished
        mSwipeRefreshLayout.setRefreshing(false);
        recyclerViewAdapter.notifyDataSetChanged();
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
                Collections.sort(artifactCards, new ArtifactSort.OrderByRank());
                recyclerViewAdapter.notifyDataSetChanged();
                break;
            case R.id.sort_state:
                Collections.sort(artifactCards, new ArtifactSort.OrderByState());
                recyclerViewAdapter.notifyDataSetChanged();
                break;
            case R.id.sort_id:
                Collections.sort(artifactCards, new ArtifactSort.OrderById());
                recyclerViewAdapter.notifyDataSetChanged();
                break;
            case R.id.sort_name:
                Collections.sort(artifactCards, new ArtifactSort.OrderByName());
                recyclerViewAdapter.notifyDataSetChanged();
                break;
            case R.id.sort_modified:
                Collections.sort(artifactCards, new ArtifactSort.OrderByModified());
                recyclerViewAdapter.notifyDataSetChanged();
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
            Collections.sort(artifactCards, new ArtifactSort.OrderByRank());
        } else if (defaultSort.equalsIgnoreCase("state")) {
            Collections.sort(artifactCards, new ArtifactSort.OrderByState());
        } else if (defaultSort.equalsIgnoreCase("id")) {
            Collections.sort(artifactCards, new ArtifactSort.OrderById());
        } else if (defaultSort.equalsIgnoreCase("name")) {
            Collections.sort(artifactCards, new ArtifactSort.OrderByName());
        } else if (defaultSort.equalsIgnoreCase("modified")) {
            Collections.sort(artifactCards, new ArtifactSort.OrderByModified());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mTracker.set(Fields.SCREEN_NAME, "Tasks");
        mTracker.send(MapBuilder.createAppView().build());
    }
}
