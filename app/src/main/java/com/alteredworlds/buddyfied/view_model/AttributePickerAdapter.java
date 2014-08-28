package com.alteredworlds.buddyfied.view_model;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.alteredworlds.buddyfied.AttributePickerFragment;

/**
 * Created by twcgilbert on 20/08/2014.
 */
public class AttributePickerAdapter extends CursorAdapter {

    final private AttributePickerFragment mPicker;

    public AttributePickerAdapter(Context context, Cursor c, int flags, AttributePickerFragment picker) {
        super(context, c, flags);
        mPicker = picker;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_checked, parent, false);
        ViewHolder holder = new ViewHolder(view, (ListView) parent);
        view.setTag(holder);
        return view;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int position = cursor.getPosition();
        ViewHolder viewHolder = (ViewHolder)view.getTag();
        viewHolder.checkedTextView.setText(cursor.getString(AttributePickerFragment.COL_ATTRIBUTE_NAME));
        Boolean checked = 1 == cursor.getInt(AttributePickerFragment.COL_ATTRIBUTE_IN_PROFILE);
        viewHolder.listView.setItemChecked(position, checked);
        if (checked)
            mPicker.setLastCheckedPosition(position);
    }

    /**
     * Cache of the child views for an attribute list item.
     */
    public static class ViewHolder {
        public final CheckedTextView checkedTextView;
        public final ListView listView;

        public ViewHolder(View view, ListView lView) {
            checkedTextView = (CheckedTextView) view.findViewById(android.R.id.text1);
            checkedTextView.setTextColor(view.getResources().getColor(android.R.color.white));
            listView = lView;
        }
    }
}
