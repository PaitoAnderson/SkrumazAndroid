package com.skrumaz.app.ui.cards;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.skrumaz.app.R;
import com.skrumaz.app.classes.Artifact;
import com.skrumaz.app.classes.Task;

import it.gmariotti.cardslib.library.internal.CardExpand;

/**
 * Created by Paito Anderson on 2014-05-19.
 */
public class ArtifactTasksCard extends CardExpand {

    private Artifact mArtifact;

    public ArtifactTasksCard(Activity activity, Artifact artifact) {
        this(activity, R.layout.card_artifact_tasks, artifact);
    }

    public ArtifactTasksCard(Activity activity, int innerLayout, Artifact artifact) {
        super(activity, innerLayout);
        mArtifact = artifact;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        if (view == null) return;

        //Retrieve TextView elements
        TextView task1 = (TextView) view.findViewById(R.id.expand_task1);

        String taskText = "";
        for (Task task : mArtifact.getTasks()) {
            if (!taskText.isEmpty()) taskText += System.getProperty("line.separator");
            taskText += task.getFormattedID() + " - " + task.getName();
        }

        if (taskText.isEmpty()) taskText += "No Tasks";

                //Set value in text views
        task1.setText(taskText);

    }
}
