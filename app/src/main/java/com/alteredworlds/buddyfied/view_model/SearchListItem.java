/*
 * Copyright (c) 2014 Tom Gilbert <tom@alteredworlds.com> - All rights reserved.
 *
 * This file is part of Buddyfied Android.
 *
 * For applicable license please see LICENSE included with this distribution.
 */

package com.alteredworlds.buddyfied.view_model;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.alteredworlds.buddyfied.R;

public class SearchListItem extends LoaderListItem {

    public SearchListItem(String name, String value, String extra, int loaderId) {
        super(name, value, extra, loaderId);
    }

    public SearchListItem(String name, String value, String extra) {
        super(name, value, extra);
    }

    @Override
    public int getViewType() {
        return ListItemID.SEARCH_VIEW_TYPE_ID;
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        SearchListItemHolder holder = null;
        View row = convertView;
        if (null == row) {
            row = (View) inflater.inflate(R.layout.list_item_search, null);
            holder = new SearchListItemHolder(row);
            row.setTag(holder);
        } else {
            holder = (SearchListItemHolder) row.getTag();
        }

        holder.nameTextView.setText(name);
        holder.valueTextView.setText(value);

        return row;
    }

    static class SearchListItemHolder {
        final TextView nameTextView;
        final TextView valueTextView;

        public SearchListItemHolder(View view) {
            nameTextView = (TextView) view.findViewById(R.id.list_item_name);
            valueTextView = (TextView) view.findViewById(R.id.list_item_value);
        }
    }
}