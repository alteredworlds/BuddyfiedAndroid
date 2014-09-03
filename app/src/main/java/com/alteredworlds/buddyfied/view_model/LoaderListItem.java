package com.alteredworlds.buddyfied.view_model;

/**
 * Created by twcgilbert on 02/09/2014.
 */
public abstract class LoaderListItem implements ListItem {
    public static final int LOADER_ID_NONE = -1;

    public static final int LOADER_ID_PLATFORM = 0;
    public static final int LOADER_ID_PLAYING = 1;
    public static final int LOADER_ID_GAMEPLAY = 2;
    public static final int LOADER_ID_COUNTRY = 3;
    public static final int LOADER_ID_LANGUAGE = 4;
    public static final int LOADER_ID_SKILL = 5;
    public static final int LOADER_ID_TIME = 6;
    public static final int LOADER_ID_AGERANGE = 7;
    public static final int LOADER_ID_VOICE = 8;

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
        this(name, value, extra, LOADER_ID_NONE);
    }
}
