package jp.s5r.android.imagesearch.util;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public final class DisplayUtil {
  private DisplayUtil() {}

  public static int convertDpToPixel(Context context, int dp) {
    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    return (int) (dp * (metrics.densityDpi / 160f));
  }


  public static int getDisplayWidth(WindowManager wm) {
    Display display = wm.getDefaultDisplay();
    if (android.os.Build.VERSION.SDK_INT < 13) {
      return display.getWidth();
    } else {
      Point size = new Point();
      display.getSize(size);
      return size.x;
    }
  }

  public static int getDisplayHeight(WindowManager wm) {
    Display display = wm.getDefaultDisplay();
    if (android.os.Build.VERSION.SDK_INT < 13) {
      return display.getHeight();
    } else {
      Point size = new Point();
      display.getSize(size);
      return size.y;
    }
  }
}
