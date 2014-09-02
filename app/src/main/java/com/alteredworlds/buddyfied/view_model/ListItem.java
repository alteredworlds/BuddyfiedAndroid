package com.alteredworlds.buddyfied.view_model;

import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by twcgilbert on 02/09/2014.
 */
public interface ListItem {
    public int getViewType();

    public View getView(LayoutInflater inflater, View convertView);
}
