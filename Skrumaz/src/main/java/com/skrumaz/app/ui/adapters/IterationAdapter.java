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
import com.skrumaz.app.classes.Iteration;
import com.skrumaz.app.utils.IterationStatusLookup;

import java.util.List;

/**
 * Created by Paito Anderson on 14-10-27.
 */
public class IterationAdapter extends RecyclerView.Adapter<IterationAdapter.ViewHolder> {

    private List<Iteration> mIterations;
    private Context mContext;

    public IterationAdapter(Context context, List<Iteration> iterations) {
        super();

        mContext = context;
        mIterations = iterations;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public CardView cardView;

        public TextView mIterationName;
        public TextView mIterationStatusName;
        public ImageView mIterationStatusColor;

        public ViewHolder(View itemView) {
            super(itemView);
            this.cardView = (CardView)itemView.findViewById(R.id.card_view);
            this.mIterationName = (TextView) itemView.findViewById(R.id.iterationName);
            this.mIterationStatusName = (TextView) itemView.findViewById(R.id.iterationStatusName);
            this.mIterationStatusColor = (ImageView) itemView.findViewById(R.id.iterationStatusColor);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        CardView view = (CardView)LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.card_iteration, viewGroup,false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.mIterationName.setText(mIterations.get(i).getName());
        viewHolder.mIterationStatusName.setText(IterationStatusLookup.iterationStatusToString(mIterations.get(i).getIterationStatus()));
        viewHolder.mIterationStatusColor.setBackgroundColor(mContext.getResources().getColor(IterationStatusLookup.iterationStatusToColor(mIterations.get(i).getIterationStatus())));
    }

    @Override
    public int getItemCount() {
        return mIterations.size();
    }
}
