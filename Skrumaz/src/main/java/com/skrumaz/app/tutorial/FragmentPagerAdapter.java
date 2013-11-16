package com.skrumaz.app.tutorial;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Paito Anderson on 11/16/2013.
 */
public class FragmentPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Integer> itemData;

    public FragmentPagerAdapter(FragmentManager fm,
                                ArrayList<Integer> itemData) {
        super(fm);
        this.itemData = itemData;
    }
    @Override
    public int getCount() {
        return itemData.size();
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }
    @Override
    public Fragment getItem(int position) {
        FragmentImageView f = FragmentImageView.newInstance();
        f.setImageList(itemData.get(position));
        return f;
    }
}
