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
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileEntry;


public class CommentEditorActivity extends Activity {
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_editor);

        mEditText = (EditText) findViewById(R.id.comment_edit_text);

        // get the initial comments value
        // NOTE: not just passing via intent because could potentially get large
        // so may as well read it from database. Easy to change either way.
        long profileId = getIntent().getLongExtra(Constants.ID_EXTRA, -1);
        Cursor cursor = getContentResolver().query(
                ProfileEntry.CONTENT_URI,
                new String[]{ProfileEntry.COLUMN_COMMENTS},
                ProfileEntry._ID + "= ?",
                new String[]{Long.toString(profileId)}, null);
        if ((null != cursor) && cursor.moveToFirst()) {
            mEditText.setText(cursor.getString(0));
        }
        cursor.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.comment_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // see comments in ProfileActivity onOptionsItemSelected
        // not necessarily safe for all cases.
        // this means that on return to the previous activity, the Intent
        // remains populated.
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void okClick(View view) {
        // we need to write the new or modified comment to the appropriate profile
        long profileId = getIntent().getLongExtra(Constants.ID_EXTRA, -1);
        String commentText = mEditText.getText().toString();

        ContentValues updatedValues = new ContentValues();
        updatedValues.put(ProfileEntry.COLUMN_COMMENTS, commentText);
        getContentResolver().update(
                ProfileEntry.CONTENT_URI,
                updatedValues,
                ProfileEntry._ID + "= ?",
                new String[]{Long.toString(profileId)});

        finish();
    }

    public void cancelClick(View view) {
        finish();
    }
}
