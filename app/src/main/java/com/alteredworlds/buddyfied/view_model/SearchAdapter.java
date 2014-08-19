package com.alteredworlds.buddyfied.view_model;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.alteredworlds.buddyfied.R;

/**
 * Created by twcgilbert on 19/08/2014.
 */
public class SearchAdapter extends ArrayAdapter<ProfileRow> {
    Context mContext;
    int mLayoutResourceId;
    ProfileRow mData[] = null;

    public SearchAdapter(Context context, int resource, ProfileRow[] objects) {
        super(context, resource, objects);
        mContext = context;
        mLayoutResourceId = resource;
        mData = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ProfileRowHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
            row = inflater.inflate(mLayoutResourceId, parent, false);

            holder = new ProfileRowHolder();
            holder.nameTextView = (TextView)row.findViewById(R.id.list_item_name);
            holder.valueTextView = (TextView)row.findViewById(R.id.list_item_value);

            row.setTag(holder);
        }
        else
        {
            holder = (ProfileRowHolder)row.getTag();
        }

        ProfileRow profileRow = mData[position];
        holder.nameTextView.setText(profileRow.name);
        holder.valueTextView.setText(profileRow.value);

        return row;
    }

    static class ProfileRowHolder
    {
        TextView nameTextView;
        TextView valueTextView;
    }
}
