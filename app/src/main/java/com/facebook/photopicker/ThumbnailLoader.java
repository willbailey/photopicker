package com.facebook.photopicker;

public interface ThumbnailLoader {
  void loadThumbnail(
      Medium medium,
      ThumbnailLoaderListener thumbnailLoaderListener);

  void clearCache();
}
