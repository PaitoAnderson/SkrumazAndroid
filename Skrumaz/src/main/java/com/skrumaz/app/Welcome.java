package com.skrumaz.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

import com.google.analytics.tracking.android.EasyTracker;
import com.skrumaz.app.tutorial.FragmentPagerAdapter;
import com.skrumaz.app.tutorial.Images;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;

/**
 * Created by Paito Anderson on 10/16/2013.
 */
public class Welcome extends FragmentActivity {

    // Stuff for Tutorial Images
    private FragmentPagerAdapter mAdapter;
    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Find all elements on the page
        Button rallySoftware = (Button) findViewById(R.id.rally_software);

        if (savedInstanceState == null) {
            // Notify handler for Rally Dev button clicks
            rallySoftware.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Sent to Login
                    startActivity(new Intent(getApplicationContext(), Login.class));
                    overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                    finish(); // Remove Activity from Stack
                }
            });

            // Tutorial Image Stuff
            Images imageId = new Images();
            ArrayList<Integer> itemData = imageId.getImageItem();

            mAdapter = new FragmentPagerAdapter(getSupportFragmentManager(), itemData);
            mPager = (ViewPager) findViewById(R.id.pager);
            mPager.setAdapter(mAdapter);

            CirclePageIndicator indicator = (CirclePageIndicator) findViewById(R.id.indicator);
            indicator.setViewPager(mPager);

            final float density = getResources().getDisplayMetrics().density;
            indicator.setRadius(5 * density);
            indicator.setPageColor(0xFF404040);
            indicator.setFillColor(0xFFFFFFFF);
            indicator.setStrokeWidth(0);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }
}