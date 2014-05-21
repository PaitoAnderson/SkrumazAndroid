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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.skrumaz.app.R;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.WebService.GetIterations;
import com.skrumaz.app.ui.factories.IterationFactory;

import java.util.ArrayList;
import java.util.List;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardGridArrayAdapter;
import it.gmariotti.cardslib.library.view.CardGridView;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.DefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created by Paito Anderson on 2014-03-16.
 */
public class Iterations extends Fragment implements OnRefreshListener {

    private CardGridView cardGridView;
    private LinearLayout processContainer;
    private ProgressBar progressSpinner;
    private CardGridArrayAdapter cardGridArrayAdapter;
    private Context mContext;
    private List<Card> iterationCards = new ArrayList<Card>();
    private PullToRefreshLayout mPullToRefreshLayout;
    private Tracker mTracker;

    public TextView progressText;
    public Boolean continueRequests = true;
    public String breakingError = "";

    public Iterations() {
        // Empty constructor required for fragment subclasses
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Set Title
        getActivity().setTitle("Iterations");

        // Get Context
        mContext = getActivity();

        // Inflate View
        View iterationsView = inflater.inflate(R.layout.activity_iteration_list, container, false);

        // Find things in the View
        cardGridView = (CardGridView) iterationsView.findViewById(R.id.listContainer);
        processContainer = (LinearLayout) iterationsView.findViewById(R.id.processContainer);
        progressText = (TextView) iterationsView.findViewById(R.id.progressText);
        progressSpinner = (ProgressBar) iterationsView.findViewById(R.id.progressSpinner);
        cardGridArrayAdapter = new CardGridArrayAdapter(getActivity().getBaseContext(), iterationCards);
        cardGridView.setAdapter(cardGridArrayAdapter);

        // Remove spinner select Workspace / Project from
        final ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        // Notify System we have our own menu items
        setHasOptionsMenu(true);

        // Pull to Refresh Library - Initialize
        mPullToRefreshLayout = (PullToRefreshLayout) iterationsView.findViewById(R.id.ptr_layout);
        ActionBarPullToRefresh.from(getActivity())
                .allChildrenArePullable()
                .listener(this)
                .setup(mPullToRefreshLayout);

        // Pull to Refresh Library - Set ProgressBar color.
        DefaultHeaderTransformer transformer = ((DefaultHeaderTransformer)mPullToRefreshLayout.getHeaderTransformer());
        transformer.setProgressBarColor(getResources().getColor(R.color.accent_color));

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
    public void onRefreshStarted(View view) {
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

            iterationCards.addAll(IterationFactory.getIterationCards(getActivity(), new GetIterations().FetchItems(mContext)));

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
                cardGridView.setVisibility(View.VISIBLE);
            }

            // Notify refresh finished
            mPullToRefreshLayout.setRefreshComplete();
            cardGridArrayAdapter.notifyDataSetChanged();

            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            // Pull Artifacts and Tasks from SQLite
            com.skrumaz.app.data.Store.Iterations db = new com.skrumaz.app.data.Store.Iterations(mContext);
            iterationCards.clear();
            iterationCards.addAll(IterationFactory.getIterationCards(getActivity(), db.getIterations(Preferences.getProjectId(mContext, true))));
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
        cardGridView.setVisibility(View.GONE);
    }

    public void finishLoading() {
        if (continueRequests) {
            // If no items display empty state
            if (iterationCards.isEmpty()) {
                progressSpinner.setVisibility(View.GONE);
                progressText.setText("No Iterations.");
            } else {
                processContainer.setVisibility(View.GONE);
                cardGridView.setVisibility(View.VISIBLE);
            }
        }

        // Notify refresh finished
        mPullToRefreshLayout.setRefreshComplete();
        cardGridArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();

        mTracker.set(Fields.SCREEN_NAME, "Iterations");
        mTracker.send(MapBuilder.createAppView().build());
    }
}
