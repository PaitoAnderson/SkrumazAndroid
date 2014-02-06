package com.skrumaz.app;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.skrumaz.app.classes.Artifact;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.utils.StatusLookup;

import java.util.List;

/**
 * Created by Paito Anderson on 2013-09-21.
 *
 * Handles the display for User Stories and Defects in an Iteration
 */
public class ArtifactAdapter extends BaseExpandableListAdapter {

    private final List<Artifact> artifacts;
    public LayoutInflater inflater;
    public Activity activity;

    public ArtifactAdapter(Activity activity, List<Artifact> artifacts) {
        this.activity = activity;
        this.artifacts = artifacts;
        this.inflater = activity.getLayoutInflater();
    }

    @Override
    public String getChild(int groupPosition, int childPosition) {
        if (Preferences.showFormattedID(activity.getBaseContext())) {
            return artifacts.get(groupPosition).getTask(childPosition).getFormattedID() + " - " + artifacts.get(groupPosition).getTask(childPosition).getName();
        } else {
            return artifacts.get(groupPosition).getTask(childPosition).getName();
        }
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String children = getChild(groupPosition, childPosition);
        TextView text;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listrow_details, null);
        }
        text = (TextView) convertView.findViewById(R.id.textViewS);
        text.setText(children);

        // On Click Task
        /*convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity, "Show Details", Toast.LENGTH_SHORT).show();
            }
        });*/
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

        ImageView artifactState = (ImageView) convertView.findViewById(R.id.imageViewH);
        TextView artifactName = (TextView) convertView.findViewById(R.id.textViewH);

        Artifact artifact = (Artifact) getGroup(groupPosition);

        if (getChildrenCount(groupPosition) == 0) {
            artifactState.setImageResource(R.drawable.navigation_clear);

            // On Click with no tasks
            /*convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(activity, "Show Details", Toast.LENGTH_SHORT).show();
                }
            });*/
        } else {
            artifactState.setImageResource(isExpanded ? R.drawable.navigation_collapse : R.drawable.navigation_expand);
        }

        // On Long Click
        /*convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(activity, "Show Details", Toast.LENGTH_SHORT).show();
                return true;
            }
        });*/

        // Set Status Icon
        Drawable status = activity.getBaseContext().getResources().getDrawable(StatusLookup.statusToRes(artifact.isBlocked(), artifact.getStatus()));
        artifactName.setCompoundDrawablesWithIntrinsicBounds(null, null, status, null);

        // Set US/DE Name
        if (Preferences.showFormattedID(activity.getBaseContext())) {
            artifactName.setText(artifact.getFormattedID() + " - " + artifact.getName());
        } else {
            artifactName.setText(artifact.getName());
        }
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
        // return true;
    }
}
