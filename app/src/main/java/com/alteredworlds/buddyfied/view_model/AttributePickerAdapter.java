/*
 * Copyright (c) 2014 Tom Gilbert <tom@alteredworlds.com> - All rights reserved.
 *
 * This file is part of Buddyfied Android.
 *
 * For applicable license please see LICENSE included with this distribution.
 */

package com.alteredworlds.buddyfied.view_model;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.alteredworlds.buddyfied.AttributePickerFragment;

public class AttributePickerAdapter extends CursorAdapter {

    final private AttributePickerFragment mPicker;
    final private int mListItemResourceId;
    private Boolean mIsFirstTime = true;

    public AttributePickerAdapter(Context context, Cursor c, int flags,
                                  Boolean singleChoice, AttributePickerFragment picker) {
        super(context, c, flags);
        mPicker = picker;
//        mListItemResourceId = singleChoice ?
//                android.R.layout.simple_list_item_single_choice :
//                android.R.layout.simple_list_item_multiple_choice;
        mListItemResourceId = android.R.layout.simple_list_item_multiple_choice;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(mListItemResourceId, parent, false);
        ViewHolder holder = new ViewHolder(view, (ListView) parent);
        view.setTag(holder);
        return view;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int position = cursor.getPosition();
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.checkedTextView.setText(cursor.getString(AttributePickerFragment.COL_ATTRIBUTE_NAME));
        Boolean checked = true;
        int columnCount = cursor.getColumnCount();
        if (cursor.getColumnCount() > AttributePickerFragment.COL_ATTRIBUTE_IN_PROFILE) {
            checked = 1 == cursor.getInt(AttributePickerFragment.COL_ATTRIBUTE_IN_PROFILE);
        }
        viewHolder.listView.setItemChecked(position, checked);
        if (checked && mIsFirstTime) {
            // initial checked value may have come from database rather than user interaction
            mPicker.setLastCheckedPosition(position);
            mIsFirstTime = false;
        }
    }

    /**
     * Cache of the child views for an attribute list item.
     */
    public static class ViewHolder {
        public final CheckedTextView checkedTextView;
        public final ListView listView;

        public ViewHolder(View view, ListView lView) {
            checkedTextView = (CheckedTextView) view.findViewById(android.R.id.text1);
            checkedTextView.setTextColor(view.getResources().getColor(android.R.color.white));
            listView = lView;
        }
    }
}
