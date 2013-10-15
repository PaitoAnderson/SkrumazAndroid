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
 */
public class ArtifactAdapter extends BaseExpandableListAdapter {

    private final List<Artifact> artifacts;
    public LayoutInflater inflater;
    public Activity activity;
    private TextView artifactName;
    private ImageView artifactState;

    public ArtifactAdapter(Activity act, List<Artifact> artifacts) {
        activity = act;
        this.artifacts = artifacts;
        inflater = act.getLayoutInflater();
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
        TextView text = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listrow_details, null);
        }
        text = (TextView) convertView.findViewById(R.id.textViewS);
        text.setText(children);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(activity, children, Toast.LENGTH_SHORT).show();
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

        artifactState = (ImageView) convertView.findViewById(R.id.imageViewH);
        artifactName = (TextView) convertView.findViewById(R.id.textViewH);

        Artifact artifact = (Artifact) getGroup(groupPosition);

        if (getChildrenCount(groupPosition) == 0) {
            //artifactState.setVisibility(View.INVISIBLE);
            artifactState.setImageResource(R.drawable.navigation_clear);
        } else {
            //artifactState.setVisibility(View.VISIBLE);
            artifactState.setImageResource(isExpanded ? R.drawable.navigation_collapse : R.drawable.navigation_expand);
        }

        // Set Status Icon
        Drawable status = activity.getBaseContext().getResources().getDrawable(StatusLookup.statusToRes(artifact.isBlocked(), artifact.getStatus()));
        artifactName.setCompoundDrawablesWithIntrinsicBounds(null, null, status, null);

        // Set US/DE Name
        if (Preferences.showFormattedID(activity.getBaseContext())) {
            artifactName.setText(artifact.getFormattedID() + " - " + artifact.getName());
        } else {
            artifactName.setText(artifact.getName());
        }
        //((CheckedTextView) convertView).setChecked(isExpanded);
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
