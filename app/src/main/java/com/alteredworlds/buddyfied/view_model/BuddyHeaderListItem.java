package com.alteredworlds.buddyfied.view_model;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.alteredworlds.buddyfied.ImageDownloader;
import com.alteredworlds.buddyfied.R;

/**
 * Created by twcgilbert on 02/09/2014.
 */
public class BuddyHeaderListItem extends LoaderListItem {

    private final Boolean mHideButtons;

    public BuddyHeaderListItem(String name, String imageUrl) {
        this(name, imageUrl, false);
    }

    public BuddyHeaderListItem(String name, String imageUrl, Boolean hideButtons) {
        super(name, imageUrl, "");
        mHideButtons = hideButtons;
    }

    @Override
    public int getViewType() {
        return ListItemID.BUDDY_HEADER_VIEW_TYPE_ID;
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        View row = convertView;

        ViewHolder holder;
        if (null == row) {
            row = inflater.inflate(R.layout.list_item_buddy_header, null);
            holder = new ViewHolder(row);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }
        holder.nameTextView.setText(name);
        if (!TextUtils.isEmpty(value)) {
            ImageDownloader.getInstance().download(value, holder.imageView, R.drawable.ic_launcher);
        }
        holder.messageButton.setVisibility(mHideButtons ? View.GONE : View.VISIBLE);
        holder.reportButton.setVisibility(mHideButtons ? View.GONE : View.VISIBLE);

        final ImageView buddyIcon = (ImageView) row.findViewById((R.id.buddy_image));
        ViewTreeObserver vto = buddyIcon.getViewTreeObserver();
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                // Remove after the first run so it doesn't fire forever
                buddyIcon.getViewTreeObserver().removeOnPreDrawListener(this);
                int finalHeight = buddyIcon.getMeasuredHeight();
                int finalWidth = buddyIcon.getMeasuredWidth();
                scaleBackground(buddyIcon, finalWidth, finalHeight);
                return true;
            }
        });
        return row;
    }

    private void scaleBackground(ImageView view, int finalWidth, int finalHeight) {
        // Change ImageView's dimensions to match the square scaled image,
        // so that the shape background actually fits it
        int scaleToSquare = (finalWidth > finalHeight) ? finalHeight : finalWidth;
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.width = scaleToSquare;
        params.height = scaleToSquare;
        view.setLayoutParams(params);
    }

    static class ViewHolder {
        final TextView nameTextView;
        final ImageView imageView;
        final ImageButton reportButton;
        final ImageButton messageButton;

        public ViewHolder(View view) {
            nameTextView = (TextView) view.findViewById(R.id.buddy_name);
            imageView = (ImageView) view.findViewById(R.id.buddy_image);
            reportButton = (ImageButton) view.findViewById(R.id.report_user_button);
            messageButton = (ImageButton) view.findViewById(R.id.message_user_button);
        }
    }
}
