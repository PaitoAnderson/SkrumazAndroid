package com.skrumaz.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.skrumaz.app.classes.User;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.Store.Users;
import com.skrumaz.app.data.WebService.GetUser;
import com.skrumaz.app.ui.Iterations;
import com.skrumaz.app.ui.Projects;
import com.skrumaz.app.ui.Search;
import com.skrumaz.app.ui.Tasks;
import com.skrumaz.app.utils.CircularImageView;
import com.uservoice.uservoicesdk.Config;
import com.uservoice.uservoicesdk.UserVoice;

/**
 * Created by Paito Anderson on 2014-03-16.
 */
public class MainActivity extends ActionBarActivity {

    private Toolbar toolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private RelativeLayout mDrawer;
    private ListView mDrawerList;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mMenuTitles;

    public enum Fragments { TASKS, ITERATIONS, PROJECTS }
    public Fragments mFragmentAttached;

    private User user;
    private CircularImageView mDrawerProfile;
    private TextView mDrawerName;
    private TextView mDrawerEmail;

    private Context mContext;

    // TODO: OnLongClickListener for Profile Picture to update User Info

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = mDrawerTitle = getTitle();
        mMenuTitles = getResources().getStringArray(R.array.menu_array);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer = (RelativeLayout) findViewById(R.id.left_drawer);
        mDrawerProfile = (CircularImageView) findViewById(R.id.left_drawer_profile);
        mDrawerName = (TextView) findViewById(R.id.left_drawer_name);
        mDrawerEmail = (TextView) findViewById(R.id.left_drawer_email);
        mDrawerList = (ListView) findViewById(R.id.left_drawer_list);

        // Set Context variable to self
        mContext = this;

        // Get User from Database
        Users users = new Users(mContext);
        user = users.getUser();

        if (user == null) {
            new UpdateUser().execute();
        } else {
            populateUser();
        }

        // Set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new DrawerAdapter(MainActivity.this, mMenuTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                toolbar.setTitle(mTitle);

                // Show Spinner
                if (mTitle.equals("")) {
                    Spinner spinner = (Spinner) findViewById(R.id.spinner);
                    spinner.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                toolbar.setTitle(mDrawerTitle);

                // Hide Spinner
                Spinner spinner = (Spinner) findViewById(R.id.spinner);
                spinner.setVisibility(View.GONE);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch (item.getItemId()) {
            case R.id.action_search:
                startActivity(new Intent(this, Search.class));
                break;
            case R.id.create_us:
                // Create User Story
                Intent createUs = new Intent(this, Create.class);
                createUs.putExtra("CreateName", mContext.getResources().getString(R.string.action_us));
                createUs.putExtra("CreateType", "HierarchicalRequirement");
                startActivity(createUs);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                break;
            case R.id.create_de:
                // Create Defect
                Intent createDe = new Intent(this, Create.class);
                createDe.putExtra("CreateName", mContext.getResources().getString(R.string.action_de));
                createDe.putExtra("CreateType", "Defect");
                startActivity(createDe);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                break;
            case R.id.create_ds:
                // Create Defect Suite
                Intent createDs = new Intent(this, Create.class);
                createDs.putExtra("CreateName", mContext.getResources().getString(R.string.action_ds));
                createDs.putExtra("CreateType", "DefectSuite");
                startActivity(createDs);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                break;
            case R.id.create_ts:
                // Create Defect Suite
                Intent createTs = new Intent(this, Create.class);
                createTs.putExtra("CreateName", mContext.getResources().getString(R.string.action_ts));
                createTs.putExtra("CreateType", "TestSet");
                startActivity(createTs);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                break;
            case R.id.create_iteration:
                // Create Iteration
                Intent createIteration = new Intent(this, Create.class);
                createIteration.putExtra("CreateName", mContext.getResources().getString(R.string.action_iteration));
                createIteration.putExtra("CreateType", "Iteration");
                startActivity(createIteration);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                break;
            case R.id.create_project:
                // Create Project
                Intent createProject = new Intent(this, Create.class);
                createProject.putExtra("CreateName", mContext.getResources().getString(R.string.action_project));
                createProject.putExtra("CreateType", "Project");
                startActivity(createProject);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                break;
            case R.id.create_workspace:
                // Create Workspace
                Intent createWorkspace = new Intent(this, Create.class);
                createWorkspace.putExtra("CreateName", mContext.getResources().getString(R.string.action_workspace));
                createWorkspace.putExtra("CreateType", "Workspace");
                startActivity(createWorkspace);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /* The click listener for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
        Fragment fragment = null;

        switch (position) {
            case 0:
                fragment = new Tasks();
                break;
            case 1:
                fragment = new Iterations();
                break;
            case 2:
                fragment = new Projects();
                break;
            case 3:
                fragment = new Settings();
                break;
            case 4:
                // UserVoice library
                UserVoice.init(new Config("skrumaz.uservoice.com"), this);

                // Launch UserVoice
                UserVoice.launchUserVoice(this);
                break;
        }

        if (fragment != null) {

            // Switch fragment
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction().replace(R.id.content_frame, fragment);
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, 0, android.R.anim.fade_out, 0);
            fragmentTransaction.commit();

            // Update title
            setTitle(mMenuTitles[position]);

            // Update selected item
            mDrawerList.setItemChecked(position, true);

            // close the drawer
            mDrawerLayout.closeDrawer(mDrawer);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        if (toolbar != null) toolbar.setTitle(mTitle);
    }

    private void populateUser() {
        if (user != null) {
            // set profile picture view
            try {
                mDrawerProfile.setImageBitmap(user.getPhotoBitmap());
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            mDrawerProfile.setBorderColor(getResources().getColor(R.color.primary_color));
            mDrawerProfile.setBorderWidth(5);
            //mDrawerProfile.addShadow();

            // set profile name
            mDrawerName.setText(user.getName());

            // set profile email
            mDrawerEmail.setText(user.getEmail());
        }
    }

    private class UpdateUser extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            // TODO: Present user with a loading spinner

            super.onPreExecute();
        }
        @Override
        protected Boolean doInBackground(String... params) {
            user = new GetUser().FetchUser(mContext);
            return null;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            // TODO: Remove loading spinner

            populateUser();

            super.onPostExecute(result);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!Preferences.isLoggedIn(mContext)) {
            Intent welcome = new Intent(this, Welcome.class);
            startActivity(welcome);
            finish(); // Remove Activity from Stack
        }
    }

    public void SetProgress(final String processMsg) {
        runOnUiThread(new Runnable() {
            public void run() {
                switch (mFragmentAttached)
                {
                    case TASKS:
                        try {
                            Tasks tasksFragment = (Tasks) getFragmentManager().findFragmentById(R.id.content_frame);
                            tasksFragment.progressText.setText(processMsg);
                        } catch (Exception ex) {
                            // Doesn't matter
                        }
                        break;
                    case ITERATIONS:
                        try {
                            Iterations iterationsFragment = (Iterations) getFragmentManager().findFragmentById(R.id.content_frame);
                            iterationsFragment.progressText.setText(processMsg);
                        } catch (Exception ex) {
                            // Doesn't matter
                        }
                        break;
                    case PROJECTS:
                        try {
                            Projects projectsFragment = (Projects) getFragmentManager().findFragmentById(R.id.content_frame);
                            projectsFragment.progressText.setText(processMsg);
                        } catch (Exception ex) {
                            // Doesn't matter
                        }
                        break;
                }
            }
        });
    }

    public void SetError(final String errorMsg) {
        switch (mFragmentAttached) {
            case TASKS:
                try {
                    Tasks tasksFragment = (Tasks) getFragmentManager().findFragmentById(R.id.content_frame);
                    tasksFragment.continueRequests = false;
                    tasksFragment.breakingError = errorMsg;
                } catch (Exception ex) {
                    // Doesn't matter
                }
                break;
            case ITERATIONS:
                try {
                    Iterations iterationsFragment = (Iterations) getFragmentManager().findFragmentById(R.id.content_frame);
                    iterationsFragment.continueRequests = false;
                    iterationsFragment.breakingError = errorMsg;
                } catch (Exception ex) {
                    // Doesn't matter
                }
                break;
            case PROJECTS:
                try {
                    Projects projectsFragment = (Projects) getFragmentManager().findFragmentById(R.id.content_frame);
                    projectsFragment.continueRequests = false;
                    projectsFragment.breakingError = errorMsg;
                } catch (Exception ex) {
                    // Doesn't matter
                }
                break;
        }
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}
