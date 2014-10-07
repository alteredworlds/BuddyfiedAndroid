/*
 * Copyright (c) 2014 Tom Gilbert <tom@alteredworlds.com> - All rights reserved.
 *
 * This file is part of Buddyfied Android.
 *
 * For applicable license please see LICENSE included with this distribution.
 */

package com.alteredworlds.buddyfied;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileEntry;

import java.util.ArrayList;

public class AgeSelectionActivity extends Activity {

    public final static String AGE_EXTRA = "age";

    private static final int NO_ROW_CHECKED = -1;
    private ArrayList<String> mItems = new ArrayList<String>();
    private ArrayAdapter<String> mAdapter;
    private int mLastCheckedPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_age_selection);

        Integer ageLower = getResources().getInteger(R.integer.age_lower_bound);
        Integer ageUpper = getResources().getInteger(R.integer.age_upper_bound);
        for (int idx = ageLower; idx < ageUpper; idx++) {
            mItems.add(String.valueOf(idx));
        }
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, mItems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextColor(getResources().getColor(R.color.white));
                return textView;
            }
        };
        mLastCheckedPosition = NO_ROW_CHECKED;
        final ListView listView = (ListView) findViewById(R.id.listview_age_picker);
        listView.setAdapter(mAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (NO_ROW_CHECKED != mLastCheckedPosition) {
                    if (position == mLastCheckedPosition) {
                        // user clicked on checked row, meaning to UN-CHECK it
                        listView.setItemChecked(position, false);
                    }
                }
                Boolean isChecked = listView.isItemChecked(position);
                setAge(position, isChecked);
                mLastCheckedPosition = isChecked ? position : NO_ROW_CHECKED;
            }
        });
        // need to set the initial check state
        String checkedValue = getIntent().getStringExtra(AGE_EXTRA);
        if (!TextUtils.isEmpty(checkedValue)) {
            for (int idx = 0; idx < mItems.size(); idx++) {
                if (0 == checkedValue.compareTo(mItems.get(idx))) {
                    listView.setItemChecked(idx, true);
                    mLastCheckedPosition = idx;
                    break;
                }
            }
        }
    }

    private void setAge(int position, Boolean isChecked) {
        // we can derive age from position
        // if isChecked is false, actually means we have NO age selection
        long profileId = getIntent().getLongExtra(Constants.ID_EXTRA, -1);
        String age = isChecked ? mAdapter.getItem(position) : null;

        ContentValues updatedValues = new ContentValues();
        updatedValues.put(ProfileEntry.COLUMN_AGE, age);
        getContentResolver().update(
                ProfileEntry.CONTENT_URI, updatedValues, ProfileEntry._ID + "= ?",
                new String[]{Long.toString(profileId)});
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.age_selection, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        // see: http://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview
        // comment by Mazvél at bottom.
        //
        // This code will override the "up" button to behave the same way as the back button so
        // in the case of Listview -> Details -> Back to Listview (and no other options) this is the
        // simplest code to maintain the scrollposition and the content in the listview.
        //
        // Caution: If you can go to another activity from the details activity the up button will
        // return you back to that activity so you will have to manipulate the backbutton history
        // in order for this to work.
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
