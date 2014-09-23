package com.alteredworlds.buddyfied.view_model;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import com.alteredworlds.buddyfied.R;

/**
 * Created by twcgilbert on 18/09/2014.
 */
public class ProfileListItem extends SearchListItem {

    public ProfileListItem(String name, String value, String extra, int loaderId) {
        super(name, value, extra, loaderId);
    }

    @Override
    public int getViewType() {
        return ListItemID.PROFILE_VIEW_TYPE_ID;
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        View row = super.getView(inflater, convertView);
        if (TextUtils.isEmpty(value)) {
            row.setBackgroundResource(R.drawable.touch_required);
        } else {
            row.setBackgroundResource(R.drawable.touch_selector);
        }
        return row;
    }
}
