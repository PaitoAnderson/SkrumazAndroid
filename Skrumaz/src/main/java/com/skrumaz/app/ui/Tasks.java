package com.skrumaz.app.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.skrumaz.app.R;

/**
 * Created by Paito Anderson on 2014-03-16.
 */
public class Tasks extends Fragment {

    private Context mContext;

    public Tasks() {
        // Empty constructor required for fragment subclasses
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Set Title
        getActivity().setTitle("Tasks");

        // Get Context
        mContext = getActivity();

        // Notify System we have our own menu items
        setHasOptionsMenu(true);

        // Return View
        return inflater.inflate(R.layout.activity_artifact_list, container, false);
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
                //Collections.sort(artifacts, new Artifact.OrderByRank());
                //adapter.notifyDataSetChanged();
                break;
            case R.id.sort_state:
                //Collections.sort(artifacts, new Artifact.OrderByState());
                //adapter.notifyDataSetChanged();
                break;
            case R.id.sort_id:
                //Collections.sort(artifacts, new Artifact.OrderById());
                //adapter.notifyDataSetChanged();
                break;
            case R.id.sort_name:
                //Collections.sort(artifacts, new Artifact.OrderByName());
                //adapter.notifyDataSetChanged();
                break;
            case R.id.sort_modified:
                //Collections.sort(artifacts, new Artifact.OrderByModified());
                //adapter.notifyDataSetChanged();
                break;
            case R.id.action_refresh:
                // Initiate API Requests
                Toast.makeText(mContext, mContext.getResources().getString(R.string.tip_swipe_down), Toast.LENGTH_LONG).show();
                //populateExpandableListView(true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
