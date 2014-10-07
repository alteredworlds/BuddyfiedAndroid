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

public interface ListItem {
    public int getViewType();

    public View getView(LayoutInflater inflater, View convertView);
}
