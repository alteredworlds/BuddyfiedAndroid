package com.alteredworlds.buddyfied.view_model;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.alteredworlds.buddyfied.AttributePickerFragment;
import com.alteredworlds.buddyfied.R;

/**
 * Created by twcgilbert on 20/08/2014.
 */
public class AttributePickerAdapter extends CursorAdapter {
    public AttributePickerAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_attribute_picker, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder)view.getTag();
        viewHolder.checkedTextView.setText(cursor.getString(AttributePickerFragment.COL_ATTRIBUTE_NAME));
        int checked = cursor.getInt(AttributePickerFragment.COL_ATTRIBUTE_IN_PROFILE);
        viewHolder.checkedTextView.setChecked(1 == checked);
    }

    /**
     * Cache of the child views for an attribute list item.
     */
    public static class ViewHolder {
        public final CheckedTextView checkedTextView;

        public ViewHolder(View view) {
            checkedTextView = (CheckedTextView) view.findViewById(R.id.list_item_attribute_value);
        }
    }
}
