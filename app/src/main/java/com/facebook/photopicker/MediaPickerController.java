package com.facebook.photopicker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.GridView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MediaPickerController implements LoaderManager.LoaderCallbacks<Cursor> {

  private static int LOADER_ID = 0;
  private final GridView mGridView;
  private final Handler mHandler;

  String[] MEDIA_PROJECTION = {
      MediaStore.Files.FileColumns._ID,
      MediaStore.Files.FileColumns.MEDIA_TYPE,
  };

  public static MediaPickerController create(Activity activity) {
    MediaPickerConfig config = new MediaPickerConfig();
    config.thumbnailLoader = new DefaultThumbnailLoader(activity);
    return MediaPickerController.create(config, activity);
  }

  public static MediaPickerController create(MediaPickerConfig config, Activity activity) {
    return new MediaPickerController(config, activity);
  }

  private final ThumbnailLoader mThumbnailLoader;
  private final Activity mActivity;
  private final View mView;
  private final CursorAdapter mAdapter;
  private boolean mMultiSelect;

  private MediaPickerController(MediaPickerConfig mediaPickerConfig, Activity activity) {
    mThumbnailLoader = mediaPickerConfig.thumbnailLoader;
    mMultiSelect = mediaPickerConfig.multiSelect;
    mActivity = activity;
    mView = LayoutInflater.from(activity).inflate(R.layout.media_picker_view, null, false);
    mHandler = mView.getHandler();
    mGridView  = (GridView) mView.findViewById(R.id.grid_view);
    mAdapter = new MediaPickerAdapter(mActivity, null, mThumbnailLoader, mGridView);
    mGridView.setAdapter(mAdapter);
  }

  public View getView() {
    return mView;
  }

  public void startLoading() {
    final LoaderManager loaderManager = mActivity.getLoaderManager();
    loaderManager.initLoader(LOADER_ID, null, this);
    mActivity.getContentResolver().registerContentObserver(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false,
        new ContentObserver(mView.getHandler()) {
          @Override
          public void onChange(boolean selfChange) {
            loaderManager.getLoader(LOADER_ID).forceLoad();
          }
        });
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
        + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

    Uri queryUri = MediaStore.Files.getContentUri("external");

    return new CursorLoader(
        mActivity,
        queryUri,
        MEDIA_PROJECTION,
        selection,
        null,
        MediaStore.Files.FileColumns.DATE_ADDED + " DESC");
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    mAdapter.changeCursor(data);
    mAdapter.notifyDataSetChanged();
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
  }
}
