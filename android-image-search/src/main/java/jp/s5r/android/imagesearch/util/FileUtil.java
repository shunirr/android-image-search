package jp.s5r.android.imagesearch.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;

public final class FileUtil {
  private FileUtil() {}

  public static File getRealFile(Context context, Uri uri) {
    if (uri != null) {
      String scheme = uri.getScheme();
      if ("file".equals(scheme)) {
        return new File(uri.getPath());
      } else if ("content".equals(scheme)) {
        return getRealFileFromContentUri(context, uri);
      }
    }

    return null;
  }

  private static File getRealFileFromContentUri(Context context, Uri uri) {
    File result = null;

    ContentResolver cr = context.getContentResolver();
    Cursor cursor = null;
    try {
      cursor = cr.query(uri, new String[] {MediaStore.Images.ImageColumns.DATA}, null, null, null);
      if (cursor != null && cursor.moveToFirst()) {
        String data = cursor.getString(0);
        if (!TextUtils.isEmpty(data)) {
          result = new File(data);
        }
      }
    } finally {
      if (cursor != null && !cursor.isClosed()) {
        cursor.close();
      }
    }

    return result;
  }
}
