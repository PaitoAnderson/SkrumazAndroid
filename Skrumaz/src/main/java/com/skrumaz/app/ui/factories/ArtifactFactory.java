package com.skrumaz.app.ui.factories;

import android.app.Activity;

import com.skrumaz.app.classes.Artifact;
import com.skrumaz.app.ui.cards.ArtifactCard;
import com.skrumaz.app.ui.cards.ArtifactTasksCard;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paito Anderson on 2014-05-19.
 */
public class ArtifactFactory {

    public static ArtifactCard getIterationCard(Activity activity, Artifact artifact) {
        ArtifactCard iterationCard = new ArtifactCard(activity, artifact);

        //CardHeader header = new CardHeader(activity.getBaseContext());
        //header.setTitle(artifact.getFormattedID());
        //header.setButtonExpandVisible(true);
        //iterationCard.addCardHeader(header);

        // Define Expand Area / Add Expand Area to Card
        ArtifactTasksCard artifactTasksCard = new ArtifactTasksCard(activity, artifact);
        iterationCard.addCardExpand(artifactTasksCard);

        return iterationCard;
    }

    public static List<ArtifactCard> getArtifactCards(Activity activity, List<Artifact> artifacts) {
        List<ArtifactCard> artifactCards = new ArrayList<ArtifactCard>();

        for(Artifact artifact: artifacts ) {
            artifactCards.add(getIterationCard(activity, artifact));
        }

        return artifactCards;
    }
}
