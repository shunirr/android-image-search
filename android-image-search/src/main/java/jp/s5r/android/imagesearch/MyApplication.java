package jp.s5r.android.imagesearch;

import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import android.app.Application;

public class MyApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();

    ImageLoaderConfiguration config =
      new ImageLoaderConfiguration.Builder(getApplicationContext())
        .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
        .build();
    ImageLoader.getInstance().init(config);
  }
}
