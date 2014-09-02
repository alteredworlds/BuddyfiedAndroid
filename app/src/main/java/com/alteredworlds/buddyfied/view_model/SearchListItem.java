package com.alteredworlds.buddyfied.view_model;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.alteredworlds.buddyfied.R;

/**
 * Created by twcgilbert on 02/09/2014.
 */
public class SearchListItem extends LoaderListItem {

    public static final int SEARCH_LIST_ITEM_VIEW_TYPE_ID = 0;

    public SearchListItem(String name, String value, String extra, int loaderId) {
        this.name = name;
        this.value = value;
        this.extra = extra;
        this.loaderId = loaderId;
    }

    @Override
    public int getViewType() {
        return SEARCH_LIST_ITEM_VIEW_TYPE_ID;
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        SearchListItemHolder holder = null;
        View row = convertView;
        if (null == row) {
            row = (View) inflater.inflate(R.layout.list_item_search, null);
            holder = new SearchListItemHolder(row);
            row.setTag(holder);
        } else {
            holder = (SearchListItemHolder) row.getTag();
        }

        holder.nameTextView.setText(name);
        holder.valueTextView.setText(value);

        return row;
    }

    static class SearchListItemHolder {
        final TextView nameTextView;
        final TextView valueTextView;

        public SearchListItemHolder(View view) {
            nameTextView = (TextView) view.findViewById(R.id.list_item_name);
            valueTextView = (TextView) view.findViewById(R.id.list_item_value);
        }
    }
}