package com.facebook.photopicker;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class MainActivity extends ActionBarActivity {

  private MediaPickerController mMediaPickerController;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mMediaPickerController = MediaPickerController.create(this);
    setContentView(mMediaPickerController.getView());
    mMediaPickerController.startLoading();
  }

}
