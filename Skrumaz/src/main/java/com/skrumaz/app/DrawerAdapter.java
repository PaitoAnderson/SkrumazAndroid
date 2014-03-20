package com.skrumaz.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by Paito Anderson on 2014-03-16.
 */
public class DrawerAdapter extends BaseAdapter {

    // Declare Variables
    Context context;
    String[] mTitle;
    LayoutInflater inflater;

    public DrawerAdapter(Context context, String[] title)
    {
        this.context = context;
        this.mTitle = title;
    }

    @Override
    public int getCount()
    {
        return mTitle.length;
    }

    @Override
    public Object getItem(int position)
    {
        return mTitle[position];
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        // Declare Variables
        View itemView;

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (position < 4)
        {
            itemView = inflater.inflate(R.layout.drawer_item_noicon, parent,
                    false);

            // Locate the TextViews in drawer_list_item_icon.xml
            TextView textView = (TextView) itemView.findViewById(R.id.text);

            // Set the title to the TextView
            textView.setText(mTitle[position]);

            // Locate the TextViews in drawer_list_item_noicon.xml
            textView = (TextView) itemView.findViewById(R.id.text);
        } else {
            itemView = inflater.inflate(R.layout.drawer_item_icon, parent,
                    false);

            // Locate the TextViews in drawer_list_item_icon.xml
            TextView textView = (TextView) itemView.findViewById(R.id.text);

            // Set the title to the TextView
            textView.setText("  " + mTitle[position].toUpperCase());

            // Set icon to the TextView
            if (position == 4) {
                textView.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_settings, 0, 0, 0);
            } else if (position == 5) {
                textView.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_help, 0, 0, 0);
            }

            // Set visibility on last items line
            View lastItemLine = itemView.findViewById(R.id.last_item_line);
            lastItemLine.setVisibility(View.VISIBLE);
        }

        return itemView;
    }
}