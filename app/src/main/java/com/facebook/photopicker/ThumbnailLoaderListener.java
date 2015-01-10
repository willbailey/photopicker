package com.facebook.photopicker;

import android.graphics.Bitmap;

public interface ThumbnailLoaderListener {
  void onThumbnailLoaded(Medium medium, boolean lowResolution, Bitmap bitmap);
}
