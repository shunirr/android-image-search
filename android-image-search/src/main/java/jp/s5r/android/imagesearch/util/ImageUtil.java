package jp.s5r.android.imagesearch.util;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public final class ImageUtil {
  private ImageUtil() {}

  public static void saveImage(File path, Bitmap bmp) throws IOException {
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(path);
      bmp.compress(Bitmap.CompressFormat.JPEG, 95, out);
      out.flush();
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }
}
