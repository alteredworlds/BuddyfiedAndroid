/*
 * Copyright (c) 2014 Tom Gilbert <tom@alteredworlds.com> - All rights reserved.
 *
 * This file is part of Buddyfied Android.
 *
 * For applicable license please see LICENSE included with this distribution.
 */

package com.alteredworlds.buddyfied.view_model;

public abstract class LoaderListItem implements ListItem {

    public String name;
    public String value;
    public String extra;
    public int loaderId;

    public LoaderListItem(String name, String value, String extra, int loaderId) {
        this.name = name;
        this.value = value;
        this.extra = extra;
        this.loaderId = loaderId;
    }

    public LoaderListItem(String name, String value, String extra) {
        this(name, value, extra, LoaderID.NONE);
    }
}
