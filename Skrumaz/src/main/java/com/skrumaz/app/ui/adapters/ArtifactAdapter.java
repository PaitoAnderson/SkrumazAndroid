package com.skrumaz.app.ui.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.skrumaz.app.R;
import com.skrumaz.app.classes.Artifact;
import com.skrumaz.app.utils.ArtifactStatusLookup;

import java.util.List;

/**
 * Created by Paito Anderson on 14-10-27.
 */
public class ArtifactAdapter extends RecyclerView.Adapter<ArtifactAdapter.ViewHolder> {

    private List<Artifact> mArtifacts;
    private Context mContext;

    public ArtifactAdapter(Context context, List<Artifact> artifacts) {
        super();

        mContext = context;
        mArtifacts = artifacts;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public CardView cardView;

        public TextView mArtifactName;
        public TextView mArtifactOwnerName;
        public TextView mArtifactStatusName;
        public ImageView mArtifactStatusColor;

        public ViewHolder(View itemView) {
            super(itemView);
            this.cardView = (CardView)itemView.findViewById(R.id.card_view);
            this.mArtifactName = (TextView) itemView.findViewById(R.id.artifactName);
            this.mArtifactOwnerName = (TextView) itemView.findViewById(R.id.artifactOwnerName);
            this.mArtifactStatusName = (TextView) itemView.findViewById(R.id.artifactStatusName);
            this.mArtifactStatusColor = (ImageView) itemView.findViewById(R.id.artifactStatusColor);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        CardView view = (CardView) LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.card_artifact, viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.mArtifactName.setText(mArtifacts.get(i).getName());
        viewHolder.mArtifactOwnerName.setText(mArtifacts.get(i).getOwnerName());
        viewHolder.mArtifactStatusName.setText(mArtifacts.get(i).getFormattedID() + " - " + ArtifactStatusLookup.statusToString(mArtifacts.get(i).getStatus()));
        viewHolder.mArtifactStatusColor.setBackgroundColor(mContext.getResources().getColor(ArtifactStatusLookup.statusToColor(mArtifacts.get(i).getStatus())));
    }

    @Override
    public int getItemCount() {
        return mArtifacts.size();
    }
}
