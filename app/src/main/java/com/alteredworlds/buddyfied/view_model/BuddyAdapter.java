package com.alteredworlds.buddyfied.view_model;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alteredworlds.buddyfied.R;

/**
 * Created by twcgilbert on 01/09/2014.
 */
public class BuddyAdapter extends SearchAdapter {
    private final int mCommentRow;

    public BuddyAdapter(Context context, int commentRow, ProfileRow[] objects) {
        super(context, R.layout.list_item_search, objects);
        mCommentRow = commentRow;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Boolean isCommentRow = (mCommentRow == position);
        mLayoutResourceId = isCommentRow ? R.layout.list_item_comments : R.layout.list_item_search;
        if (isCommentRow) {
            convertView = null;
        }
        return super.getView(position, convertView, parent);
    }

    @Override
    protected ProfileRowHolder newRowHolder(View view, int resourceId) {
        if (resourceId == R.layout.list_item_comments) {
            ProfileRowHolder holder = new ProfileRowHolder();
            holder.nameTextView = (TextView) view.findViewById(R.id.list_item_comments_name);
            holder.valueTextView = (TextView) view.findViewById(R.id.list_item_comments_value);
            return holder;
        } else {
            return super.newRowHolder(view, resourceId);
        }
    }
}
