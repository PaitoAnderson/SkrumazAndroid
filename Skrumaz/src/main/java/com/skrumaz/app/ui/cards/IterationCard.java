package com.skrumaz.app.ui.cards;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.skrumaz.app.R;
import com.skrumaz.app.classes.Iteration;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.ui.Tasks;
import com.skrumaz.app.utils.IterationStatusLookup;

import it.gmariotti.cardslib.library.internal.Card;

/**
 * Created by Paito Anderson on 2014-05-18.
 */
public class IterationCard extends Card {

    private Iteration mIteration;

    protected TextView mIterationName;
    protected TextView mIterationStatusName;
    protected ImageView mIterationStatusColor;

    public IterationCard(Activity activity, Iteration iteration) {
        this(activity, R.layout.card_iteration, iteration);
    }

    public IterationCard(final Activity activity, int innerLayout, final Iteration iteration) {
        super(activity, innerLayout);
        mIteration = iteration;

        // Setup On Card Click Listener
        setOnClickListener(new OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                // Set Iteration Id to use
                Preferences.setIterationId(activity.getBaseContext(), iteration.getOid());

                // Send to Task List
                Fragment fragment = new Tasks();
                FragmentManager fragmentManager = activity.getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().replace(R.id.content_frame, fragment);
                fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, 0, android.R.anim.fade_out, 0);
                fragmentTransaction.addToBackStack("Iterations");
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        // Set Iteration Data Elements to Card

        mIterationName = (TextView) parent.findViewById(R.id.iterationName);
        mIterationStatusName = (TextView) parent.findViewById(R.id.iterationStatusName);
        mIterationStatusColor = (ImageView) parent.findViewById(R.id.iterationStatusColor);

        mIterationName.setText(mIteration.getName());
        mIterationStatusName.setText(IterationStatusLookup.iterationStatusToString(mIteration.getIterationStatus()));
        mIterationStatusColor.setBackgroundColor(getContext().getResources().getColor(IterationStatusLookup.iterationStatusToColor(mIteration.getIterationStatus())));
    }
}
