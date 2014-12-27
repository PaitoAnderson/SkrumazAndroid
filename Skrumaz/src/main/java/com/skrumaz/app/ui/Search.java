package com.skrumaz.app.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.skrumaz.app.R;
import com.skrumaz.app.classes.Artifact;
import com.skrumaz.app.classes.Project;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.WebService.GetQueryResults;
import com.skrumaz.app.ui.adapters.ArtifactAdapter;
import com.skrumaz.app.utils.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paito Anderson on 14-12-26.
 */
public class Search extends ActionBarActivity {

    private static final String TAG = "SEARCH";

    private Context mContext;
    private List<Artifact> artifactCards = new ArrayList<Artifact>();

    private RecyclerView recyclerView;
    private RecyclerView.Adapter recyclerViewAdapter;

    private LinearLayout processContainer;
    private ProgressBar progressSpinner;

    private Tracker mTracker;

    SearchView mSearchView = null;
    private String mQuery = "";

    public TextView progressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Set Context
        mContext = this;

        // Find in View
        recyclerView = (RecyclerView) findViewById(R.id.searchList);
        processContainer = (LinearLayout) findViewById(R.id.processContainer);
        progressText = (TextView) findViewById(R.id.progressText);
        progressSpinner = (ProgressBar) findViewById(R.id.progressSpinner);
        recyclerViewAdapter = new ArtifactAdapter(getBaseContext(), artifactCards);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Setup Card Clicks
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(mContext, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {

                        // Send to Task View
                        Intent viewTask = new Intent(mContext, com.skrumaz.app.ui.Artifact.class);
                        viewTask.putExtra("ArtifactName", artifactCards.get(position).getFormattedID());
                        startActivity(viewTask);
                        overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                    }
                })
        );

        // Setup Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Add back button icon
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);

        // Passed in Query
        String query = getIntent().getStringExtra(SearchManager.QUERY);
        query = query == null ? "" : query;
        mQuery = query;

        // Google Analytics
        mTracker = EasyTracker.getInstance(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.search, menu);
        final MenuItem searchItem = menu.findItem(R.id.menu_search);
        if (searchItem != null) {
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            final SearchView view = (SearchView) searchItem.getActionView();
            mSearchView = view;
            if (view != null) {
                view.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
                view.setIconified(false);
                view.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        view.clearFocus();

                        mQuery = query;

                        new GetService().execute();

                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        return true;
                    }
                });
                view.setOnCloseListener(new SearchView.OnCloseListener() {
                    @Override
                    public boolean onClose() {
                        finish();
                        return false;
                    }
                });

                if (!TextUtils.isEmpty(mQuery)) {
                    new GetService().execute();
                }
            }
        }
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mQuery = intent.getStringExtra(SearchManager.QUERY);
            if (!TextUtils.isEmpty(mQuery)) {
                if (mSearchView != null) mSearchView.setQuery(mQuery, true);
            }
        }
    }

    private class GetService extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected void onPreExecute() {

            artifactCards.clear();

            processContainer.setVisibility(View.VISIBLE);
            progressSpinner.setVisibility(View.VISIBLE);
            progressText.setVisibility(View.VISIBLE);
            progressText.setText("Searching...");
            recyclerView.setVisibility(View.GONE);

            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(String... params) {

            artifactCards.addAll(new GetQueryResults().FetchItems(mContext, mQuery));

            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            // If no items display empty state
            if (artifactCards.isEmpty()) {
                progressSpinner.setVisibility(View.GONE);

                // Query Current Project from Database
                com.skrumaz.app.data.Store.Projects projectsData = new com.skrumaz.app.data.Store.Projects(getBaseContext());
                Project project = projectsData.getProject(Preferences.getProjectId(getBaseContext(), true));

                if (project != null) {
                    progressText.setText("No results found for \"" + mQuery + "\" in project \"" + project.getName() + "\".");
                } else {
                    progressText.setText("No results found for \"" + mQuery + "\".");
                }
            } else {
                processContainer.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }

            // Notify of new data
            recyclerViewAdapter.notifyDataSetChanged();

            super.onPostExecute(result);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_search:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        mTracker.set(Fields.SCREEN_NAME, "Search");
        mTracker.send(MapBuilder.createAppView().build());
    }
}
