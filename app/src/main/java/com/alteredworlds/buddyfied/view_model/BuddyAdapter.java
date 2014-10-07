/*
 * Copyright (c) 2014 Tom Gilbert <tom@alteredworlds.com> - All rights reserved.
 *
 * This file is part of Buddyfied Android.
 *
 * For applicable license please see LICENSE included with this distribution.
 */

package com.alteredworlds.buddyfied.view_model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class BuddyAdapter extends ArrayAdapter<ListItem> {
    private LayoutInflater mInflater;

    // NOTE: these constants must all be distinct values
    public int[] RowType = new int[]{
            ListItemID.SEARCH_VIEW_TYPE_ID,
            ListItemID.COMMENTS_VIEW_TYPE_ID,
            ListItemID.BUDDY_HEADER_VIEW_TYPE_ID,
            ListItemID.PROFILE_VIEW_TYPE_ID
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
