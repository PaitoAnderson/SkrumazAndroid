package com.skrumaz.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.skrumaz.app.classes.Iteration;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.ui.Tasks;
import com.skrumaz.app.utils.IterationStatusLookup;

import java.util.List;

/**
 * Created by Paito Anderson on 11/23/2013.
 *
 * Handles the display for a list of Iterations
 */
public class IterationAdapter extends BaseAdapter {

    private final List<Iteration> iterations;
    private LayoutInflater inflater;
    private Activity activity;

    public IterationAdapter(Activity activity, List<Iteration> iterations) {
        this.activity = activity;
        this.iterations = iterations;
        this.inflater = activity.getLayoutInflater();
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

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listrow_iteration, null);
        }

        ImageView iterationStatus = (ImageView) convertView.findViewById(R.id.iterationStatus);
        TextView iterationName = (TextView) convertView.findViewById(R.id.iterationName);

        iterationName.setText(iteration.getName());
        iterationStatus.setImageResource(IterationStatusLookup.iterationStatusToRes(iteration.getIterationStatus()));

        // Respond to clicking
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set Iteration Id to use
                Preferences.setIterationId(activity, iteration.getOid());

                // Send to Task List
                Fragment fragment = new Tasks();
                FragmentManager fragmentManager = activity.getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().replace(R.id.content_frame, fragment);
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, 0, android.R.anim.fade_out, 0);
                fragmentTransaction.addToBackStack("Iterations");
                fragmentTransaction.commit();
            }
        });

        return convertView;
    }
}
