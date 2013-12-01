package com.skrumaz.app;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.skrumaz.app.classes.Iteration;
import com.skrumaz.app.data.Preferences;

import java.util.List;

/**
 * Created by Paito Anderson on 11/23/2013.
 *
 * Handles the display for a list of Iterations
 */
public class IterationAdapter extends BaseAdapter {

    private final List<Iteration> iterations;
    public LayoutInflater inflater;
    public Activity activity;
    private TextView iterationName;
    private TextView iterationDateRange;

    public IterationAdapter(Activity act, List<Iteration> iterations) {
        activity = act;
        this.iterations = iterations;
        inflater = act.getLayoutInflater();
    }

    @Override
    public int getCount() {
        return iterations.size();
    }

    @Override
    public Iteration getItem(int position) {
        return iterations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return iterations.get(position).getOid();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Iteration iteration = getItem(position);

        TextView text = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listrow_iteration, null);
        }
        text = (TextView) convertView.findViewById(R.id.textViewS);
        text.setText(iteration.getName());

        // Respond to clicking
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set Iteration Id to use
                Preferences.setIterationId(activity, iteration.getOid());

                // Send to Artifact List
                activity.startActivity(new Intent(activity, ArtifactList.class));
                activity.overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);

                //Toast.makeText(activity, iteration.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }
}