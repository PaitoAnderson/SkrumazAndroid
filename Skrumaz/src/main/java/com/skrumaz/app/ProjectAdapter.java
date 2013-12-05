package com.skrumaz.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.skrumaz.app.classes.Project;

import java.util.List;

/**
 * Created by Paito Anderson on 12/1/2013.
 */
public class ProjectAdapter extends ArrayAdapter<Project> {

    private final List<Project> projects;
    private Context context;

    public ProjectAdapter(Context context, int resource, int textViewResourceId, List<Project> projects) {
        super(context, resource, textViewResourceId, projects);
        this.context = context;
        this.projects = projects;
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

    /*
     * Passive Spinner State
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Project project = getItem(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView text = (TextView) inflater.inflate(R.layout.spinner_item, null);

        text.setText(project.getName());

        return text;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        final Project project = getItem(position);

        if  (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.spinner_dropdown_item, parent, false);
        }

        TextView projectName = (TextView) convertView.findViewById(R.id.projectName);

        projectName.setText(project.getName());

        return convertView;
    }
}
