package com.alteredworlds.buddyfied.view_model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * Created by twcgilbert on 01/09/2014.
 */

public class BuddyAdapter extends ArrayAdapter<ListItem> {
    private LayoutInflater mInflater;

    // NOTE: these constants must all be distinct values
    public int[] RowType = new int[]{
            SearchListItem.SEARCH_LIST_ITEM_VIEW_TYPE_ID,
            CommentsListItem.COMMENTS_LIST_ITEM_VIEW_TYPE_ID
    };

    public BuddyAdapter(Context context, ListItem[] objects) {
        super(context, 0, objects);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getViewTypeCount() {
        return RowType.length;

    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getViewType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getItem(position).getView(mInflater, convertView);
    }
}
