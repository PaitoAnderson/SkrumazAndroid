package com.skrumaz.app;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.skrumaz.app.classes.Artifact;

import java.util.List;

/**
 * Created by Paito Anderson on 2013-09-21.
 */
public class ArtifactAdapter extends BaseExpandableListAdapter {

    private final List<Artifact> artifacts;
    public LayoutInflater inflater;
    public Activity activity;

    public ArtifactAdapter(Activity act, List<Artifact> artifacts) {
        activity = act;
        this.artifacts = artifacts;
        inflater = act.getLayoutInflater();
    }

    @Override
    public String getChild(int groupPosition, int childPosition) {
        return artifacts.get(groupPosition).getTask(childPosition).getName();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String children = getChild(groupPosition, childPosition);
        TextView text = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listrow_details, null);
        }
        text = (TextView) convertView.findViewById(R.id.textView1);
        text.setText(children);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity, children,
                        Toast.LENGTH_SHORT).show();
            }
        });
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return artifacts.get(groupPosition).getTasks().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return artifacts.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return artifacts.size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listrow_group, null);
        }

        if (getChildrenCount(groupPosition) == 0) {
            //convertView.setEnabled(false);
        }

        Artifact artifact = (Artifact) getGroup(groupPosition);
        ((CheckedTextView) convertView).setText(artifact.getName());
        ((CheckedTextView) convertView).setChecked(isExpanded);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
