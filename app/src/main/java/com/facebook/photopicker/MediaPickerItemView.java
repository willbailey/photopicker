package com.facebook.photopicker;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class MediaPickerItemView extends FrameLayout {

  private static final String TAG = MediaPickerItemView.class.getSimpleName();

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
  private static final int THREADS = Runtime.getRuntime().availableProcessors() / 2;
  private static final ExecutorService LOADING_EXECUTOR = new ThreadPoolExecutor(
      THREADS,
      THREADS,
      0L,
      TimeUnit.MILLISECONDS,
      WORK_QUEUE);
  private static final int CACHE_SIZE = (int) (Runtime.getRuntime().maxMemory() / 8);
  private static final LruCache<Integer, Bitmap> BITMAP_CACHE =
      new LruCache<Integer, Bitmap>(CACHE_SIZE){
        protected int sizeOf(Integer key, Bitmap bitmap) {
          Log.d(
              TAG,
              "kb: " + bitmap.getByteCount() / 1024 +
                  " w: " + bitmap.getWidth() +
                  " h: " + bitmap.getHeight());
          return bitmap.getByteCount();
        }
      };

  private final ImageView mImage;
  private final BitmapFactory.Options mBitmapOptions;
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

    mBitmapOptions = new BitmapFactory.Options();
    mBitmapOptions.inSampleSize = 2;

    ViewGroup root = (ViewGroup) LayoutInflater.from(context).inflate(
        R.layout.media_picker_item_view, this, false);

    mImage = (ImageView) root.findViewById(R.id.image);

    addView(root);
  }

  public void bind(final Medium medium) {
    if (mId == medium.id) {
      return;
    }
    mId = medium.id;
    mMedium = medium;

    mImage.setImageBitmap(null);

    if (BITMAP_CACHE.get(medium.id) != null) {
      mImage.setImageBitmap(BITMAP_CACHE.get(medium.id));
    } else {
      LOADING_EXECUTOR.submit(new Runnable() {
        @Override
        public void run() {
          final Bitmap thumb = MediaStore.Images.Thumbnails.getThumbnail(
              getContext().getContentResolver(),
              medium.id,
              MediaStore.Video.Thumbnails.MINI_KIND,
              mBitmapOptions);
          BITMAP_CACHE.put(medium.id, thumb);

          post(new Runnable() {
            @Override
            public void run() {
              if (mId == medium.id) {
                mImage.setImageBitmap(thumb);
              }
            }
          });

        }
      });
    }
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
