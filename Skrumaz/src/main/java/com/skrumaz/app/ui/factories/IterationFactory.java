package com.skrumaz.app.ui.factories;

import android.app.Activity;

import com.skrumaz.app.classes.Iteration;
import com.skrumaz.app.ui.cards.IterationCard;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paito Anderson on 2014-05-18.
 */
public class IterationFactory {

    public static IterationCard getIterationCard(Activity activity, Iteration iteration) {
        IterationCard iterationCard = new IterationCard(activity, iteration);

        //CardHeader header = new CardHeader(activity.getBaseContext());
        //header.setTitle(iteration.getName());
        //iterationCard.addCardHeader(header);

        return iterationCard;
    }

    public static List<IterationCard> getIterationCards(Activity activity, List<Iteration> iterations) {
        List<IterationCard> iterationCards = new ArrayList<IterationCard>();

        for(Iteration iteration: iterations ) {
            iterationCards.add(getIterationCard(activity, iteration));
        }

        return iterationCards;
    }
}
