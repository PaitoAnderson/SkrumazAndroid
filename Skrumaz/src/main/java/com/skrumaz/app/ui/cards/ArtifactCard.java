package com.skrumaz.app.ui.cards;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.skrumaz.app.R;
import com.skrumaz.app.classes.Artifact;
import com.skrumaz.app.utils.ArtifactStatusLookup;

import java.util.Comparator;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.ViewToClickToExpand;

/**
 * Created by Paito Anderson on 2014-05-19.
 */
public class ArtifactCard extends Card {

    private Artifact mArtifact;

    public ArtifactCard(Activity activity, Artifact artifact) {
        this(activity, R.layout.card_artifact, artifact);
    }

    public ArtifactCard(final Activity activity, int innerLayout, final Artifact artifact) {
        super(activity, innerLayout);
        mArtifact = artifact;
    }

    public static class OrderByRank implements Comparator<Card> {

        @Override
        public int compare(Card o1, Card o2) {
            return ((ArtifactCard) o1).mArtifact.getRank().compareTo(((ArtifactCard) o2).mArtifact.getRank());
        }
    }

    public static class OrderByState implements Comparator<Card> {

        @Override
        public int compare(Card o1, Card o2) {
            return ((ArtifactCard) o2).mArtifact.getStatus().compareTo(((ArtifactCard) o1).mArtifact.getStatus());
        }
    }

    public static class OrderById implements Comparator<Card> {

        @Override
        public int compare(Card o1, Card o2) {
            return ((ArtifactCard) o2).mArtifact.getFormattedID().compareTo(((ArtifactCard) o1).mArtifact.getFormattedID());
        }
    }

    public static class OrderByName implements Comparator<Card> {

        @Override
        public int compare(Card o1, Card o2) {
            return ((ArtifactCard) o1).mArtifact.getName().compareTo(((ArtifactCard) o2).mArtifact.getName());
        }
    }

    public static class OrderByModified implements Comparator<Card> {

        @Override
        public int compare(Card o1, Card o2) {
            return ((ArtifactCard) o2).mArtifact.getLastUpdate().compareTo(((ArtifactCard) o1).mArtifact.getLastUpdate());
        }
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        // Set Artifact Data Elements to Card

        TextView artifactName = (TextView) parent.findViewById(R.id.artifactName);
        TextView artifactStatusName = (TextView) parent.findViewById(R.id.artifactStatusName);
        TextView artifactOwnerName = (TextView) parent.findViewById(R.id.artifactOwnerName);
        ImageView artifactStatusColor = (ImageView) parent.findViewById(R.id.artifactStatusColor);

        artifactName.setText(mArtifact.getName());
        artifactStatusName.setText(mArtifact.getFormattedID() + " - " + ArtifactStatusLookup.statusToString(mArtifact.getStatus()));
        artifactOwnerName.setText(mArtifact.getOwnerName());
        artifactStatusColor.setBackgroundColor(getContext().getResources().getColor(ArtifactStatusLookup.statusToColor(mArtifact.getStatus())));

        // On Click Expand Section
        ViewToClickToExpand viewToClickToExpand = ViewToClickToExpand.builder().setupView(view);
        setViewToClickToExpand(viewToClickToExpand);
    }
}