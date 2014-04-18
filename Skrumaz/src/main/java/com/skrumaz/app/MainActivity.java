package com.skrumaz.app;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.skrumaz.app.classes.User;
import com.skrumaz.app.data.Preferences;
import com.skrumaz.app.data.Store.Users;
import com.skrumaz.app.data.WebService.GetUser;
import com.skrumaz.app.ui.Iterations;
import com.skrumaz.app.ui.Projects;
import com.skrumaz.app.ui.Tasks;
import com.skrumaz.app.utils.CircularImageView;
import com.uservoice.uservoicesdk.Config;
import com.uservoice.uservoicesdk.UserVoice;

/**
 * Created by Paito Anderson on 2014-03-16.
 */
public class MainActivity extends Activity {
    private DrawerLayout mDrawerLayout;
    private RelativeLayout mDrawer;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mMenuTitles;

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

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

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

        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new DrawerAdapter(MainActivity.this, mMenuTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
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
            case R.id.action_settings:
                // Launch Setting Activity
                startActivity(new Intent(this, Settings.class));
                break;
            case R.id.action_help:
                // UserVoice library
                UserVoice.init(new Config("skrumaz.uservoice.com"), this);

                // Launch UserVoice
                UserVoice.launchUserVoice(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /* The click listner for ListView in the navigation drawer */
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
                Intent intent = new Intent(this, Settings.class);
                startActivity(intent);
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
        getActionBar().setTitle(mTitle);
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
