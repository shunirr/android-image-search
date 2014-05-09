package jp.s5r.android.imagesearch.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public final class Config {
  private Config() {}

  public static boolean useTiqav(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    return prefs.getBoolean("prefs_use_tiqav", false);
  }

  public static boolean saveGallery(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    return prefs.getBoolean("prefs_save_gallery", false);
  }
}
