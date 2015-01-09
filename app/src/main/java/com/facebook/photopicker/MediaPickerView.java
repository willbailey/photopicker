package com.facebook.photopicker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MediaPickerView extends FrameLayout implements LoaderManager.LoaderCallbacks<Cursor> {

  String[] MEDIA_PROJECTION = {
      MediaStore.Files.FileColumns._ID,
      MediaStore.Files.FileColumns.DATA,
      MediaStore.Files.FileColumns.DATE_ADDED,
      MediaStore.Files.FileColumns.MEDIA_TYPE,
      MediaStore.Files.FileColumns.MIME_TYPE,
      MediaStore.Files.FileColumns.TITLE
  };

  private final CursorAdapter mAdapter;

  public MediaPickerView(Context context) {
    this(context, null);
  }

  public MediaPickerView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MediaPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    ViewGroup container =
        (ViewGroup) LayoutInflater.from(context).inflate(R.layout.media_picker_view, this, false);
    addView(container);

    GridView gridView = (GridView) container.findViewById(R.id.grid_view);
    mAdapter = new MediaPickerAdapter(getActivity(), null, true);
    gridView.setAdapter(mAdapter);

    LoaderManager loaderManager = getActivity().getLoaderManager();
    loaderManager.initLoader(0, null, this);
  }

  private Activity getActivity() {
    return (Activity) getContext();
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "="
        + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
//        + " OR "
//        + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
//        + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

    Uri queryUri = MediaStore.Files.getContentUri("external");

    return new CursorLoader(
        getActivity(),
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
