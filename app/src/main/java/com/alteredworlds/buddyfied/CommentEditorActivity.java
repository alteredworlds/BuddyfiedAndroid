package com.alteredworlds.buddyfied;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.alteredworlds.buddyfied.data.BuddyfiedContract;
import com.alteredworlds.buddyfied.data.BuddyfiedContract.ProfileEntry;


public class CommentEditorActivity extends Activity {
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_editor);

        mEditText = (EditText) findViewById(R.id.comment_edit_text);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.comment_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void okClick(View view) {
        // we need to write the new or modified comment to the appropriate profile
        long profileId = getIntent().getLongExtra(Constants.ID_EXTRA, -1);
        String commentText = mEditText.getText().toString();

        ContentValues updatedValues = new ContentValues();
        updatedValues.put(BuddyfiedContract.BuddyEntry.COLUMN_COMMENTS, commentText);
        getContentResolver().update(
                ProfileEntry.CONTENT_URI, updatedValues, ProfileEntry._ID + "= ?",
                new String[]{Long.toString(profileId)});

        finish();
    }

    public void cancelClick(View view) {
        finish();
    }
}
