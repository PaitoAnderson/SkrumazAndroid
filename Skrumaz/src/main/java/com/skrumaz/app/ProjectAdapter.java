package com.skrumaz.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.skrumaz.app.classes.Project;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.ui.Iterations;

import java.util.List;

/**
 * Created by Paito Anderson on 2014-03-15.
 */
public class ProjectAdapter extends BaseAdapter {

    private final List<Project> projects;
    private LayoutInflater inflater;
    private Activity activity;

    public ProjectAdapter(Activity act, List<Project> projects) {
        activity = act;
        this.projects = projects;
        inflater = act.getLayoutInflater();
    }

    @Override
    public int getCount() {
        return projects.size();
    }

    @Override
    public Project getItem(int position) {
        return projects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return projects.get(position).getOid();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Project project = getItem(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listrow_project, null);
        }

        // Set Project name
        TextView projectName = (TextView) convertView.findViewById(R.id.projectName);
        projectName.setText(project.getName());

        // Respond to clicking
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set Project Id to use
                Preferences.setProjectId(activity, project.getOid());

                // Send to Iteration List
                Fragment fragment = new Iterations();
                FragmentManager fragmentManager = activity.getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().replace(R.id.content_frame, fragment);
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, 0, android.R.anim.fade_out, 0);
                fragmentTransaction.addToBackStack("Projects");
                fragmentTransaction.commit();
            }
        });

        return convertView;
    }
}
