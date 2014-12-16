package com.skrumaz.app.ui.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.skrumaz.app.R;
import com.skrumaz.app.classes.Task;

import java.util.List;

/**
 * Created by Paito Anderson on 14-11-09.
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private List<Task> mTasks;

    public TaskAdapter(List<Task> tasks) {
        super();

        mTasks = tasks;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public CardView cardView;
        public TextView mTaskName;

        public ViewHolder(View itemView) {
            super(itemView);
            this.cardView = (CardView) itemView.findViewById(R.id.card_view);
            this.mTaskName = (TextView) itemView.findViewById(R.id.taskName);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        CardView view = (CardView) LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.card_task, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.mTaskName.setText(mTasks.get(i).getName());
    }

    @Override
    public int getItemCount() {
        return mTasks.size();
    }
}
