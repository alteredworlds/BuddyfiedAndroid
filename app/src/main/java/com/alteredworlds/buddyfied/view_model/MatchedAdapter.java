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
import android.widget.ImageView;
import android.widget.TextView;

import com.alteredworlds.buddyfied.ImageDownloader;
import com.alteredworlds.buddyfied.MatchedFragment;
import com.alteredworlds.buddyfied.R;

public class MatchedAdapter extends CursorAdapter {

    public MatchedAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_matched, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (null != cursor) {
            String name = cursor.getString(MatchedFragment.COL_NAME);
            viewHolder.textView.setText(name);
            String imageUrl = cursor.getString(MatchedFragment.COL_IMAGE_URI);
            ImageDownloader.getInstance().download(imageUrl, viewHolder.imageView, R.drawable.avatar);
        }
    }

    /**
     * Cache of the child views for an attribute list item.
     */
    public static class ViewHolder {
        public final TextView textView;
        public final ImageView imageView;

        public ViewHolder(View view) {
            textView = (TextView) view.findViewById(R.id.list_item_matched_text);
            imageView = (ImageView) view.findViewById(R.id.list_item_matched_image);
        }
    }
}
