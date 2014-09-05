package com.alteredworlds.buddyfied.view_model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
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
        Drawable drawing = view.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) drawing).getBitmap();

        // Get current dimensions
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int testWidth = pxToDp(width, view) * 10;
        int testHeight = pxToDp(height, view) * 10;

        int scaleToSquare = (finalWidth > finalHeight) ? finalHeight : finalWidth;
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.width = scaleToSquare;
        params.height = scaleToSquare;
        view.setLayoutParams(params);
    }

    private int pxToDp(int px, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        float density = displayMetrics.density;
        return Math.round((float) px / density);
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
