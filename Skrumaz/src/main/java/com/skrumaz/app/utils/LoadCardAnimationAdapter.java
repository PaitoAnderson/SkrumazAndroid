package com.skrumaz.app.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * Created by Paito Anderson on 2014-05-21.
 */
public class LoadCardAnimationAdapter extends SwingBottomInAnimationAdapter {

    public LoadCardAnimationAdapter(BaseAdapter baseAdapter) {
        super(baseAdapter);
    }

    @Override
    protected Animator getAnimator(final ViewGroup parent, final View view) {
        return ObjectAnimator.ofFloat(view, "translationY", 500, 0).ofFloat(view, "alpha", 0.25f, 1);
    }
}
