package com.facebook.photopicker;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import java.util.HashSet;

public class MediaPickerAdapter extends CursorAdapter {

  private final HashSet<Integer> mSelections = new HashSet<>();
  private final ThumbnailLoader mThumbnailLoader;
  private final Activity mActivity;
  private final View mRootView;

  public MediaPickerAdapter(Activity activity, Cursor c, ThumbnailLoader thumbnailLoader, View rootView) {
    super(activity, c, false);
    mActivity = activity;
    mThumbnailLoader = thumbnailLoader;
    mRootView = rootView;
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
    final MediaPickerItemView itemView = new MediaPickerItemView(context);
    bindView(itemView, context, cursor);
    itemView.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            int id = itemView.getMedium().id;
            if (mSelections.contains(id)) {
              mSelections.remove(id);
            } else {
              mSelections.add(id);
            }
            itemView.setPicked(mSelections.contains(id));
          }
        });
    return itemView;
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor) {
    MediaPickerItemView itemView = (MediaPickerItemView) view;
    Medium medium = new Medium();
    medium.id = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
    itemView.bind(medium, mThumbnailLoader);
    itemView.setPicked(mSelections.contains(medium.id));
    int dim = Math.round(mRootView.getWidth() / 3f);
    itemView.setDimensions(dim);
  }

}
