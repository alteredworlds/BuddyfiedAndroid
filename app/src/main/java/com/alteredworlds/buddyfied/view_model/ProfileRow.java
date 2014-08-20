package com.alteredworlds.buddyfied.view_model;

/**
 * Created by twcgilbert on 19/08/2014.
 */
public class ProfileRow {
    public String name;
    public String value;
    public String attributeType;
    public int loaderId;

    public ProfileRow() {
        super();
    }

    public ProfileRow(String name, String value, String attributeType, int loaderId) {
        super();
        this.name = name;
        this.value = value;
        this.attributeType = attributeType;
        this.loaderId = loaderId;
    }
}
