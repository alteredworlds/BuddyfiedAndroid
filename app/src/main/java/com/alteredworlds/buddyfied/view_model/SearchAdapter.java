package com.alteredworlds.buddyfied.view_model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * Created by twcgilbert on 19/08/2014.
 */
public class SearchAdapter extends ArrayAdapter<ListItem> {
    private LayoutInflater mInflater;

    public SearchAdapter(Context context, ListItem[] objects) {
        super(context, 0, objects);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getItem(position).getView(mInflater, convertView);
    }
}
