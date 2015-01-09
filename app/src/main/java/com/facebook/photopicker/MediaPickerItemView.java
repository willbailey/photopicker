package com.facebook.photopicker;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class MediaPickerItemView extends FrameLayout {

  private static final int PICKED_COLOR = Color.argb(100, 0, 0, 255);
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
  private static final int CACHE_SIZE = (int) (Runtime.getRuntime().maxMemory() / 8);

  private static final LruCache<Integer, Bitmap> SMALL_THUMBNAIL_BITMAP_CACHE =
      new LruCache<Integer, Bitmap>(CACHE_SIZE){
        protected int sizeOf(Integer key, Bitmap bitmap) {
          return bitmap.getByteCount();
        }
      };

  private static final LruCache<Integer, Bitmap> LARGE_THUMBNAIL_BITMAP_CACHE =
      new LruCache<Integer, Bitmap>(CACHE_SIZE){
        protected int sizeOf(Integer key, Bitmap bitmap) {
          return bitmap.getByteCount();
        }
      };

  private final ImageView mImage;
  private final BitmapFactory.Options mLargeThumbnailBitmapOptions;
  private final BitmapFactory.Options mSmallThumbnailBitmapOptions;
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

    mLargeThumbnailBitmapOptions = new BitmapFactory.Options();
    mLargeThumbnailBitmapOptions.inSampleSize = 2;
    mSmallThumbnailBitmapOptions = new BitmapFactory.Options();
    mSmallThumbnailBitmapOptions.inSampleSize = 6;

    mImage = (ImageView) LayoutInflater.from(context).inflate(
        R.layout.media_picker_item_view, this, false);

    addView(mImage);
  }

  public void bind(final Medium medium) {
    if (mId == medium.id) {
      return;
    }
    mId = medium.id;
    mMedium = medium;
    mImage.setVisibility(INVISIBLE);
    loadThumbnails(medium);
  }

  private void loadThumbnails(final Medium medium) {
    mImage.setImageBitmap(SMALL_THUMBNAIL_BITMAP_CACHE.get(medium.id));
    mImage.setVisibility(VISIBLE);
    if (LARGE_THUMBNAIL_BITMAP_CACHE.get(medium.id) != null) {
      mImage.setImageBitmap(LARGE_THUMBNAIL_BITMAP_CACHE.get(medium.id));
      mImage.setVisibility(VISIBLE);
    } else if (SMALL_THUMBNAIL_BITMAP_CACHE.get(medium.id) != null) {
      LOADING_EXECUTOR.submit(new Runnable() {
        @Override
        public void run() {
          loadLargeThumbnail(medium);
        }
      });
    } else {
      LOADING_EXECUTOR.submit(new Runnable() {
        @Override
        public void run() {
          loadSmallThumbnail(medium);
          loadLargeThumbnail(medium);
        }
      });
    }
  }

  private void loadSmallThumbnail(final Medium medium) {
    final Bitmap smallThumb = MediaStore.Images.Thumbnails.getThumbnail(
        getContext().getContentResolver(),
        medium.id,
        MediaStore.Video.Thumbnails.MINI_KIND,
        mSmallThumbnailBitmapOptions);
    SMALL_THUMBNAIL_BITMAP_CACHE.put(medium.id, smallThumb);
    post(new Runnable() {
      @Override
      public void run() {
        if (mId == medium.id) {
          mImage.setImageBitmap(smallThumb);
          mImage.setVisibility(VISIBLE);
        }
      }
    });
  }

  private void loadLargeThumbnail(final Medium medium) {
    final Bitmap thumb = MediaStore.Images.Thumbnails.getThumbnail(
        getContext().getContentResolver(),
        medium.id,
        MediaStore.Video.Thumbnails.MINI_KIND,
        mLargeThumbnailBitmapOptions);
    LARGE_THUMBNAIL_BITMAP_CACHE.put(medium.id, thumb);
    post(new Runnable() {
      @Override
      public void run() {
        if (mId == medium.id) {
          mImage.setImageBitmap(thumb);
          mImage.setVisibility(VISIBLE);
        }
      }
    });
  }

  public void setPicked(boolean picked) {
    if (picked) {
      mImage.setColorFilter(PICKED_COLOR);
    } else {
      mImage.clearColorFilter();
    }
    mImage.invalidate();
  }

  public Medium getMedium() {
    return mMedium;
  }
}
