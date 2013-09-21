package com.skrumaz.app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.SparseArray;
import android.view.Menu;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

    ExpandableListView listView;
    LinearLayout layout;
    SparseArray<Group> userStories = new SparseArray<Group>();
    UserStoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ExpandableListView) findViewById(R.id.listContainer);
        layout = (LinearLayout) findViewById(R.id.processContainer);
        adapter = new UserStoryAdapter(this, userStories);
        listView.setAdapter(adapter);

        // UserVoice
        //Config config = new Config("skrumaz.uservoice.com");
        //UserVoice.init(config, this);

        //UserVoice.launchForum(this);

        new Task().execute();
    }

    class Task extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            layout.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            layout.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            for (int j = 0; j < 5; j++) {
                Group group = new Group("US2311 - Sylectus (90479) - Testing QuickBooks Imports with updated User Interface Part " + j);
                for (int i = 0; i < 5; i++) {
                    group.children.add("Testing QuickBooks Imports with updated User Interface Part " + i);
                }
                userStories.append(j, group);
            }

            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
