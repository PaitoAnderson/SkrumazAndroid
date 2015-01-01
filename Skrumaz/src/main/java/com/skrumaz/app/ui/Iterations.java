package com.skrumaz.app.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.skrumaz.app.R;
import com.skrumaz.app.classes.Iteration;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.WebService.GetIterations;
import com.skrumaz.app.ui.adapters.IterationAdapter;
import com.skrumaz.app.utils.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paito Anderson on 2014-03-16.
 */
public class Iterations extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private Context mContext;
    private List<Iteration> iterationCards = new ArrayList<>();

    private RecyclerView recyclerView;
    private IterationAdapter recyclerViewAdapter;

    private LinearLayout processContainer;
    private ProgressBar progressSpinner;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private Tracker mTracker;

    public TextView progressText;
    public Boolean continueRequests = true;
    public String breakingError = "";

    public Iterations() {
        // Empty constructor required for fragment subclasses
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Find Spinner and hide
        Spinner spinner = (Spinner) getActivity().findViewById(R.id.spinner);
        spinner.setVisibility(View.GONE);

        // Set Title
        getActivity().setTitle("Iterations");

        // Get Context
        mContext = getActivity();

        // Inflate View
        View iterationsView = inflater.inflate(R.layout.activity_iteration_list, container, false);

        // Find things in the View
        recyclerView = (RecyclerView) iterationsView.findViewById(R.id.iterationList);
        processContainer = (LinearLayout) iterationsView.findViewById(R.id.processContainer);
        progressText = (TextView) iterationsView.findViewById(R.id.progressText);
        progressSpinner = (ProgressBar) iterationsView.findViewById(R.id.progressSpinner);
        recyclerViewAdapter = new IterationAdapter(getActivity().getBaseContext(), iterationCards);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Setup Card Clicks
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(mContext, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        // Set Iteration Id to use
                        Preferences.setIterationId(mContext, iterationCards.get(position).getOid());

                        // Send to Task List
                        Fragment fragment = new Tasks();
                        FragmentManager fragmentManager = getActivity().getFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().replace(R.id.content_frame, fragment);
                        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, 0, android.R.anim.fade_out, 0);
                        fragmentTransaction.addToBackStack("Iterations");
                        fragmentTransaction.commit();
                    }
                })
        );

        // Setup Loading Animation
        //AnimationAdapter animCardArrayAdapter = new LoadCardAnimationAdapter(cardGridArrayAdapter);
        //animCardArrayAdapter.setAbsListView(cardGridView);
        //cardGridView.setExternalAdapter(animCardArrayAdapter, cardGridArrayAdapter);

        // Remove spinner select Workspace / Project from
        //final ActionBar actionBar = getActivity().getActionBar();
        //actionBar.setDisplayShowTitleEnabled(true);
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        // Notify System we have our own menu items
        setHasOptionsMenu(true);

        // Swipe to Refresh Library - Initialize
        mSwipeRefreshLayout = (SwipeRefreshLayout) iterationsView.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.accent_color);

        // Return View
        return iterationsView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.iteration, menu);
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Initiate API Requests
        populateListView(false);

        // Google Analytics
        mTracker = EasyTracker.getInstance(getActivity());
    }

    @Override
    public void onRefresh() {
        populateListView(true);
    }

    protected void populateListView(boolean forceRefresh) {
        com.skrumaz.app.data.Store.Iterations db = new com.skrumaz.app.data.Store.Iterations(mContext);
        if (forceRefresh || db.isValidIterations(Preferences.getProjectId(mContext, true))) {
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

            iterationCards.addAll(new GetIterations().FetchItems(mContext));

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
            if (iterationCards.isEmpty()) {
                progressSpinner.setVisibility(View.GONE);
                progressText.setText("No Iterations.");
            } else {
                processContainer.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            // Notify refresh finished
            mSwipeRefreshLayout.setRefreshing(false);
            recyclerViewAdapter.notifyDataSetChanged();

            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            // Pull Artifacts and Tasks from SQLite
            com.skrumaz.app.data.Store.Iterations db = new com.skrumaz.app.data.Store.Iterations(mContext);
            iterationCards.clear();
            iterationCards.addAll(db.getIterations(Preferences.getProjectId(mContext, true)));
            db.close();

            return null;
        }
    }

    public void startLoading() {
        // Reset Views / Spinner
        iterationCards.clear();
        processContainer.setVisibility(View.VISIBLE);
        progressSpinner.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
        progressText.setText("Getting Items..."); // Text updated using SetProgress()
        recyclerView.setVisibility(View.GONE);
    }

    public void finishLoading() {
        if (continueRequests) {
            // If no items display empty state
            if (iterationCards.isEmpty()) {
                progressSpinner.setVisibility(View.GONE);
                progressText.setText("No Iterations.");
            } else {
                processContainer.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }

        // Notify refresh finished
        mSwipeRefreshLayout.setRefreshing(false);
        recyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();

        mTracker.set(Fields.SCREEN_NAME, "Iterations");
        mTracker.send(MapBuilder.createAppView().build());
    }
}
