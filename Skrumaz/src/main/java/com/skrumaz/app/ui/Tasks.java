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
import com.skrumaz.app.data.Store.Artifacts;
import com.skrumaz.app.data.WebService.GetArtifacts;
import com.skrumaz.app.ui.cards.ArtifactCard;
import com.skrumaz.app.ui.factories.ArtifactFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.gmariotti.cardslib.library.extra.staggeredgrid.internal.CardGridStaggeredArrayAdapter;
import it.gmariotti.cardslib.library.extra.staggeredgrid.view.CardGridStaggeredView;
import it.gmariotti.cardslib.library.internal.Card;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.DefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

/**
 * Created by Paito Anderson on 2014-03-16.
 */
public class Tasks extends Fragment implements OnRefreshListener {

    private CardGridStaggeredView cardGridStaggeredView;
    private LinearLayout processContainer;
    private ProgressBar progressSpinner;
    private CardGridStaggeredArrayAdapter cardGridStaggeredArrayAdapter;
    private Context mContext;
    private List<Card> artifactCards = new ArrayList<Card>();
    private PullToRefreshLayout mPullToRefreshLayout;
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
        cardGridStaggeredView = (CardGridStaggeredView) tasksView.findViewById(R.id.listContainer);
        processContainer = (LinearLayout) tasksView.findViewById(R.id.processContainer);
        progressText = (TextView) tasksView.findViewById(R.id.progressText);
        progressSpinner = (ProgressBar) tasksView.findViewById(R.id.progressSpinner);
        cardGridStaggeredArrayAdapter = new CardGridStaggeredArrayAdapter(getActivity().getBaseContext(), artifactCards);
        cardGridStaggeredView.setAdapter(cardGridStaggeredArrayAdapter);

        // Remove spinner select Workspace / Project from
        final ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        // Disable Group Indicator
        //listView.setGroupIndicator(null);

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

        // Google Analytics
        mTracker = EasyTracker.getInstance(getActivity());
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

            artifactCards.addAll(ArtifactFactory.getArtifactCards(getActivity(), new GetArtifacts().FetchItems(mContext)));

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
                cardGridStaggeredView.setVisibility(View.VISIBLE);

                // Sort Artifacts
                sortByDefault();
            }

            // Notify refresh finished
            mPullToRefreshLayout.setRefreshComplete();
            cardGridStaggeredArrayAdapter.notifyDataSetChanged();

            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {

            // Pull Artifacts and Tasks from SQLite
            Artifacts db = new Artifacts(mContext);
            artifactCards.clear();
            artifactCards.addAll(ArtifactFactory.getArtifactCards(getActivity(), db.getArtifacts(Preferences.getIterationId(mContext, true))));
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
        cardGridStaggeredView.setVisibility(View.GONE);
    }

    public void finishLoading() {
        if (continueRequests) {
            // If no items display empty state
            if (artifactCards.isEmpty()) {
                progressSpinner.setVisibility(View.GONE);
                progressText.setText("No Items in Current Iteration.");
            } else {
                processContainer.setVisibility(View.GONE);
                cardGridStaggeredView.setVisibility(View.VISIBLE);

                // Sort Artifacts
                sortByDefault();
            }
        }

        // Notify refresh finished
        mPullToRefreshLayout.setRefreshComplete();
        cardGridStaggeredArrayAdapter.notifyDataSetChanged();
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
                Collections.sort(artifactCards, new ArtifactCard.OrderByRank());
                cardGridStaggeredArrayAdapter.notifyDataSetChanged();
                break;
            case R.id.sort_state:
                Collections.sort(artifactCards, new ArtifactCard.OrderByState());
                cardGridStaggeredArrayAdapter.notifyDataSetChanged();
                break;
            case R.id.sort_id:
                Collections.sort(artifactCards, new ArtifactCard.OrderById());
                cardGridStaggeredArrayAdapter.notifyDataSetChanged();
                break;
            case R.id.sort_name:
                Collections.sort(artifactCards, new ArtifactCard.OrderByName());
                cardGridStaggeredArrayAdapter.notifyDataSetChanged();
                break;
            case R.id.sort_modified:
                Collections.sort(artifactCards, new ArtifactCard.OrderByModified());
                cardGridStaggeredArrayAdapter.notifyDataSetChanged();
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
            Collections.sort(artifactCards, new ArtifactCard.OrderByRank());
        } else if (defaultSort.equalsIgnoreCase("state")) {
            Collections.sort(artifactCards, new ArtifactCard.OrderByState());
        } else if (defaultSort.equalsIgnoreCase("id")) {
            Collections.sort(artifactCards, new ArtifactCard.OrderById());
        } else if (defaultSort.equalsIgnoreCase("name")) {
            Collections.sort(artifactCards, new ArtifactCard.OrderByName());
        } else if (defaultSort.equalsIgnoreCase("modified")) {
            Collections.sort(artifactCards, new ArtifactCard.OrderByModified());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mTracker.set(Fields.SCREEN_NAME, "Tasks");
        mTracker.send(MapBuilder.createAppView().build());
    }
}
