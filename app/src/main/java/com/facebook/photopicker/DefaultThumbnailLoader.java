package com.facebook.photopicker;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.MediaStore;
import android.util.LruCache;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class DefaultThumbnailLoader implements ThumbnailLoader {

  private static final BlockingQueue<Runnable> WORK_QUEUE = new LinkedBlockingDeque<Runnable>() {
    @Override
    public boolean offer(Runnable runnable) {
      return super.offerFirst(runnable);
    }

    @Override
    public Runnable remove() {
      return super.removeFirst();
    }
  };

  private static final int THREADS = Math.round(Runtime.getRuntime().availableProcessors() / 2f);

  private static final ExecutorService LOADING_EXECUTOR = new ThreadPoolExecutor(
      THREADS,
      THREADS,
      0L,
      TimeUnit.MILLISECONDS,
      WORK_QUEUE);

  private static final LruCache<Integer, Bitmap> LOW_RES_CACHE =
      new LruCache<Integer, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 10)){
        protected int sizeOf(Integer key, Bitmap bitmap) {
          return bitmap.getByteCount();
        }
      };

  private static final LruCache<Integer, Bitmap> HIGH_RES_CACHE =
      new LruCache<Integer, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 8)){
        protected int sizeOf(Integer key, Bitmap bitmap) {
          return bitmap.getByteCount();
        }
      };

  private final BitmapFactory.Options mHighResOptions;
  private final BitmapFactory.Options mLowResOptions;
  private final Activity mActivity;

  public DefaultThumbnailLoader(Activity activity) {
    mActivity = activity;
    mHighResOptions = new BitmapFactory.Options();
    mHighResOptions.inSampleSize = 1;
    mLowResOptions = new BitmapFactory.Options();
    mLowResOptions.inSampleSize = 4;
  }

  @Override
  public void loadThumbnail(
      final Medium medium,
      final ThumbnailLoaderListener thumbnailLoaderListener) {
    if (HIGH_RES_CACHE.get(medium.id) != null) {
      thumbnailLoaderListener.onThumbnailLoaded(medium, false, HIGH_RES_CACHE.get(medium.id));
    } else if (LOW_RES_CACHE.get(medium.id) != null) {
      thumbnailLoaderListener.onThumbnailLoaded(medium, true, LOW_RES_CACHE.get(medium.id));
      LOADING_EXECUTOR.submit(new Runnable() {
        @Override
        public void run() {
          loadThumbnail(medium, false, thumbnailLoaderListener);
        }
      });
    } else {
      LOADING_EXECUTOR.submit(new Runnable() {
        @Override
        public void run() {
          loadThumbnail(medium, true, thumbnailLoaderListener);
          loadThumbnail(medium, false, thumbnailLoaderListener);
        }
      });
    }
  }

  private void loadThumbnail(
      final Medium medium,
      final boolean lowResolution,
      final ThumbnailLoaderListener thumbnailLoaderListener) {
    BitmapFactory.Options options = lowResolution ? mLowResOptions : mHighResOptions;
    LruCache<Integer, Bitmap> cache = lowResolution ? LOW_RES_CACHE : HIGH_RES_CACHE;
    final Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(
        mActivity.getContentResolver(),
        medium.id,
        MediaStore.Video.Thumbnails.MINI_KIND,
        options);
    cache.put(medium.id, bitmap);
    mActivity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        thumbnailLoaderListener.onThumbnailLoaded(medium, lowResolution, bitmap);
      }
    });
  }

  @Override
  public void clearCache() {
    HIGH_RES_CACHE.evictAll();
    LOW_RES_CACHE.evictAll();
  }
}
