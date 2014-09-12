package com.alteredworlds.buddyfied;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.alteredworlds.buddyfied.service.StaticDataService;

import java.lang.reflect.Constructor;


public class MainActivity extends ActionBarActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private BroadcastReceiver mMessageReceiver;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mMainMenuTitles;
    private TypedArray mMainMenuIcons;
    private String[] mMainMenuFragmentNames;
    private int mPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = mDrawerTitle = getTitle();
        mMainMenuTitles = getResources().getStringArray(R.array.main_menu_names);
        mMainMenuIcons = getResources().obtainTypedArray(R.array.main_menu_icons);
        mMainMenuFragmentNames = getResources().getStringArray(R.array.main_menu_fragments);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        //
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, R.id.drawer_list_item_textview, mMainMenuTitles) {


            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ViewHolder viewHolder = (ViewHolder) view.getTag();
                if (null == viewHolder) {
                    viewHolder = new ViewHolder(view);
                    view.setTag(viewHolder);
                }
                viewHolder.iconView.setImageDrawable(mMainMenuIcons.getDrawable(position));
                return view;
            }
        });
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // find the selected item information & produce suitably formatted string
                selectItem(position);
            }
        });

        // need to verify if this behaviour actually meets Android standards
        // w.r.t. use and meaning of the 'Back' button. I suspect it does not.
        int position = Settings.getPosition(this);
        if ((null == savedInstanceState) || (mPosition != position)) {
            selectItem(position);
        }
        //
        // pull static data if needed
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle results = intent.getBundleExtra(Constants.RESULT_BUNDLE);
                if (null != results) {
                    int code = results.getInt(Constants.RESULT_CODE, Constants.RESULT_OK);
                    String description = results.getString(Constants.RESULT_DESCRIPTION, "");
                    if ((Constants.RESULT_FAIL == code) && !Utils.isNullOrEmpty(description)) {
                        // error case, e.g.: problem with server, network
                        Toast toast = Toast.makeText(context, description, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }
        };
        Intent staticDataIntent = new Intent(this, StaticDataService.class);
        staticDataIntent.putExtra(Constants.METHOD_EXTRA, StaticDataService.GET_ALL_IF_NEEDED);
        startService(staticDataIntent);
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {
        // Create a new fragment based on position
        Fragment fragment = fragmentFactoryNewFragmentForPosition(position);
        //
        // Insert the fragment by replacing any existing fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mMainMenuTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private Fragment fragmentFactoryNewFragmentForPosition(int position) {
        Fragment retVal = null;
        String fragmentName = mMainMenuFragmentNames[position];
        try {
            Class fragmentClass = Class.forName(fragmentName);
            Constructor constructor = fragmentClass.getConstructor();
            retVal = (Fragment) constructor.newInstance();
            mPosition = position;
            Settings.setPosition(this, position);
        } catch (Exception exception) {
            Log.e(LOG_TAG,
                    "Failed to find suitable class " + fragmentName + " for menu position " + position);
        }
        return retVal;
    }

    @Override
    public void setTitle(CharSequence title) {
        if (null != title) {
            mTitle = title;
            getActionBar().setTitle(mTitle);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        //
        // Register an observer to receive specific named Intents ('events')
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(StaticDataService.STATIC_DATA_SERVICE_RESULT_EVENT));
    }

    @Override
    public void onPause() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            refreshStaticData();
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshStaticData() {
        Intent staticDataIntent = new Intent(this, StaticDataService.class);
        staticDataIntent.putExtra(Constants.METHOD_EXTRA, StaticDataService.UPDATE_ALL);
        startService(staticDataIntent);
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current state
        //
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onSelectMenuItem(String title) {
        // find the position from the title, then swap fragments to the desired one
        int position = -1;
        for (int i = 0; i < mMainMenuTitles.length; i++) {
            if (0 == mMainMenuTitles[i].compareTo(title)) {
                position = i;
                break;
            }
        }
        if (-1 != position) {
            selectItem(position);
        }
    }

    public void matchedButtonClick(View view) {
        onSelectMenuItem("Matched");
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public static class ViewHolder {
        public final ImageView iconView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.drawer_list_item_icon);
        }
    }
}
