package com.facebook.photopicker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class PhotoFrame extends View {

  private final float mCellPadding;
  private final Paint mPaint;

  public PhotoFrame(Context context) {
    this(context, null);
  }

  public PhotoFrame(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PhotoFrame(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    mCellPadding = getResources().getDimension(R.dimen.cell_padding);
    mPaint = new Paint();
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeWidth(mCellPadding);
    mPaint.setColor(Color.argb(255, 255, 255, 255));
    mPaint.setAntiAlias(true);
    canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mPaint);
  }

}
