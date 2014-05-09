package jp.s5r.android.imagesearch.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

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

  public static Uri addGarally(Context context, File path) throws IOException {
    ContentValues values = new ContentValues();
    ContentResolver contentResolver = context.getContentResolver();
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
    values.put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000);
    values.put(MediaStore.Images.Media.SIZE, path.length());
    values.put(MediaStore.Images.Media.TITLE, path.getName());
    values.put(MediaStore.Images.Media.DATA, path.getPath());
    return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
  }
}
