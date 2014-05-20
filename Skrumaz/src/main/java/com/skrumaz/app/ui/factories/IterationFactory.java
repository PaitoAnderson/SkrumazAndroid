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

    public static List<IterationCard> getIterationCards(Activity activity, List<Iteration> iterations) {
        List<IterationCard> iterationCards = new ArrayList<IterationCard>();

        for(Iteration iteration: iterations ) {
            iterationCards.add(new IterationCard(activity, iteration));
        }

        return iterationCards;
    }
}
