package com.alteredworlds.buddyfied.view_model;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.alteredworlds.buddyfied.R;

/**
 * Created by twcgilbert on 02/09/2014.
 */
public class CommentsListItem extends LoaderListItem {

    public CommentsListItem(String name, String value, String extra) {
        super(name, value, extra);
    }

    @Override
    public int getViewType() {
        return ListItemID.COMMENTS_VIEW_TYPE_ID;
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