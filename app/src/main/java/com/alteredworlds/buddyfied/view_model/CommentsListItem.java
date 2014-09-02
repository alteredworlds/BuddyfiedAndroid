package com.alteredworlds.buddyfied.view_model;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.alteredworlds.buddyfied.R;

/**
 * Created by twcgilbert on 02/09/2014.
 */
public class CommentsListItem extends LoaderListItem {

    public static final int COMMENTS_LIST_ITEM_VIEW_TYPE_ID = 1;

    public CommentsListItem(String name, String value, String extra, int loaderId) {
        this.name = name;
        this.value = value;
        this.extra = extra;
        this.loaderId = loaderId;
    }

    @Override
    public int getViewType() {
        return COMMENTS_LIST_ITEM_VIEW_TYPE_ID;
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        CommentsListItemHolder holder = null;
        View row = convertView;
        if (null == row) {
            row = (View) inflater.inflate(R.layout.list_item_comments, null);
            holder = new CommentsListItemHolder(row);
            row.setTag(holder);
        } else {
            holder = (CommentsListItemHolder) row.getTag();
        }

        holder.nameTextView.setText(name);
        holder.valueTextView.setText(value);

        return row;
    }

    static class CommentsListItemHolder {
        final TextView nameTextView;
        final TextView valueTextView;

        public CommentsListItemHolder(View view) {
            nameTextView = (TextView) view.findViewById(R.id.list_item_comments_name);
            valueTextView = (TextView) view.findViewById(R.id.list_item_comments_value);
        }
    }
}