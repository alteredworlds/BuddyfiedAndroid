package com.alteredworlds.buddyfied.view_model;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alteredworlds.buddyfied.ImageDownloader;
import com.alteredworlds.buddyfied.R;
import com.alteredworlds.buddyfied.Utils;

/**
 * Created by twcgilbert on 02/09/2014.
 */
public class BuddyHeaderListItem extends LoaderListItem {

    public static final int LIST_ITEM_VIEW_TYPE_ID = 2;

    public BuddyHeaderListItem(String name, String imageUrl) {
        super(name, imageUrl, "");
    }

    @Override
    public int getViewType() {
        return LIST_ITEM_VIEW_TYPE_ID;
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        View row = convertView;

        ViewHolder holder = null;
        if (null == row) {
            row = (View) inflater.inflate(R.layout.list_item_buddy_header, null);
            holder = new ViewHolder(row);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }
        holder.nameTextView.setText(name);
        if (!Utils.isNullOrEmpty(value)) {
            ImageDownloader.getInstance().download(value, holder.imageView);
        }

        return row;
    }

    static class ViewHolder {
        final TextView nameTextView;
        final ImageView imageView;

        public ViewHolder(View view) {
            nameTextView = (TextView) view.findViewById(R.id.buddy_name);
            imageView = (ImageView) view.findViewById(R.id.buddy_image);
        }
    }
}
