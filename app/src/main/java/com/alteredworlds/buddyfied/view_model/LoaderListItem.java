package com.alteredworlds.buddyfied.view_model;

/**
 * Created by twcgilbert on 02/09/2014.
 */
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
