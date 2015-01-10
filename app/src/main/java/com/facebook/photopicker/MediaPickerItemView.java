package com.facebook.photopicker;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class MediaPickerItemView extends FrameLayout implements ThumbnailLoaderListener {

  private static Map<Integer, LayoutParams> PARAMS_CACHE = new HashMap<Integer,LayoutParams>();
  private final PhotoFrame mFrame;

  private static FrameLayout.LayoutParams getCachedLayoutParams(int dim) {
    LayoutParams params = PARAMS_CACHE.get(dim);
    if (params == null) {
      params = new LayoutParams(dim, dim);
      params.gravity = Gravity.CENTER;
      PARAMS_CACHE.put(dim, params);
    }
    return params;
  }

  private static final int PICKED_COLOR = Color.argb(100, 0, 0, 255);

  private final ImageView mImage;
  private int mId;
  private Medium mMedium;

  public MediaPickerItemView(Context context) {
    this(context, null);
  }

  public MediaPickerItemView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MediaPickerItemView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setWillNotDraw(false);
    ViewGroup container = (ViewGroup) LayoutInflater.from(context).inflate(
        R.layout.media_picker_item_view, this, false);
    mImage = (ImageView) container.findViewById(R.id.image);
    mFrame = (PhotoFrame) container.findViewById(R.id.frame);
    addView(container);
  }

  public void bind(final Medium medium, ThumbnailLoader thumbnailLoader) {
    if (mId == medium.id) {
      return;
    }
    mId = medium.id;
    mMedium = medium;
    mImage.setVisibility(INVISIBLE);
    thumbnailLoader.loadThumbnail(medium, this);
  }

  public void setPicked(boolean picked) {
    if (picked) {
      mImage.setColorFilter(PICKED_COLOR);
    } else {
      mImage.clearColorFilter();
    }
    mImage.invalidate();
  }

  @Override
  public void onThumbnailLoaded(Medium medium, boolean lowResolution, Bitmap bitmap) {
    if (medium.id != mId) {
      return;
    }
    mImage.setVisibility(VISIBLE);
    mImage.setImageBitmap(bitmap);
    invalidate();
  }

  public Medium getMedium() {
    return mMedium;
  }

  public void setDimensions(int dim) {
    mImage.setLayoutParams(getCachedLayoutParams(dim));
    mFrame.setLayoutParams(getCachedLayoutParams(dim));
  }

}
