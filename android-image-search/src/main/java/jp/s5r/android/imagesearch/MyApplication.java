package jp.s5r.android.imagesearch;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import android.app.Application;

public class MyApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();

    ImageLoaderConfiguration config =
      new ImageLoaderConfiguration.Builder(getApplicationContext())
        .build();
    ImageLoader.getInstance().init(config);
  }
}
