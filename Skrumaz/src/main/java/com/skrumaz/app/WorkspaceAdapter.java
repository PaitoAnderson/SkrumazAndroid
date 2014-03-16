package com.skrumaz.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.skrumaz.app.classes.Workspace;

import java.util.List;

/**
 * Created by Paito Anderson on 12/1/2013.
 */
public class WorkspaceAdapter extends ArrayAdapter<Workspace> {

    private final List<Workspace> workspaces;
    private Context context;

    public WorkspaceAdapter(Context context, int resource, int textViewResourceId, List<Workspace> workspaces) {
        super(context, resource, textViewResourceId, workspaces);
        this.context = context;
        this.workspaces = workspaces;
    }

    @Override
    public int getCount() {
        return workspaces.size();
    }

    @Override
    public Workspace getItem(int position) {
        return workspaces.get(position);
    }

    @Override
    public long getItemId(int position) {
        return workspaces.get(position).getOid();
    }

    /*
     * Passive Spinner State
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Workspace workspace = getItem(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView text = (TextView) inflater.inflate(R.layout.spinner_item, null);

        text.setText(workspace.getName());

        return text;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        final Workspace workspace = getItem(position);

        if  (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.spinner_dropdown_item, parent, false);
        }

        TextView workspaceName = (TextView) convertView.findViewById(R.id.workspaceName);

        workspaceName.setText(workspace.getName());

        return convertView;
    }
}
