package jp.s5r.android.imagesearch.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import java.io.File;

public final class IntentUtil {
  private IntentUtil() {}

  public static void shareImage(Context context, File file) {
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("image/jpeg");
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
    try {
      context.startActivity(intent);
    } catch (ActivityNotFoundException e) {
      Toast.makeText(context, "Failed to launch app.", Toast.LENGTH_SHORT).show();
    }
  }
}
