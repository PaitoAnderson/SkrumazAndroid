package com.skrumaz.app.ui;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.skrumaz.app.R;
import com.skrumaz.app.classes.Task;
import com.skrumaz.app.data.Store.Artifacts;
import com.skrumaz.app.ui.adapters.TaskAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paito Anderson on 14-11-02.
 */
public class Artifact extends ActionBarActivity {

    // TAG for logging
    private static final String TAG = "ARTIFACT";

    private Context mContext;

    private List<Task> taskCards = new ArrayList<Task>();

    private LinearLayout viewContainer;
    private TextView artifactTitle;
    private WebView artifactDescription;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter recyclerViewAdapter;

    private String artifactName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artifact);

        Bundle extras = getIntent().getExtras();
        artifactName = extras.getString("ArtifactName");

        // Find Objects in View
        viewContainer = (LinearLayout) findViewById(R.id.viewContainer);
        artifactTitle = (TextView) findViewById(R.id.artifactTitle);
        artifactDescription = (WebView) findViewById(R.id.artifactDescription);
        recyclerView = (RecyclerView) findViewById(R.id.taskList);
        recyclerViewAdapter = new TaskAdapter(this.getBaseContext(), taskCards);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Set Context variable to self
        mContext = this;

        // Setup Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Add back button icon
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);

        // Add Data
        com.skrumaz.app.classes.Artifact artifact = new Artifacts(this).getArtifact(artifactName);
        taskCards.addAll(artifact.getTasks());
        actionbar.setTitle(artifact.getFormattedID().trim() + " - " + artifact.getName().trim());
        artifactTitle.setText(artifact.getName());
        artifactDescription.loadDataWithBaseURL("", artifact.getDescription(), "text/html", "UTF-8", "");

        // Notify adapter we added tasks
        recyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action buttons
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

}
