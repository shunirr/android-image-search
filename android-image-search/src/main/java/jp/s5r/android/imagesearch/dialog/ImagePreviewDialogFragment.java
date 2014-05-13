package jp.s5r.android.imagesearch.dialog;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import jp.s5r.android.imagesearch.model.ImageModel;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

public class ImagePreviewDialogFragment extends DialogFragment implements ImageLoadingListener {

  public interface OnDialogButtonListener {
    void onClickShare(ImageModel model);
    void onClickCancel(ImageModel model);
  }

  private ImageModel mImageModel;
  private ImageView mImageView;
  private OnDialogButtonListener mOnDialogButtonListener;

  public ImagePreviewDialogFragment(ImageModel imageModel) {
    mImageModel = imageModel;
  }

  public void setOnDialogButtonListener(OnDialogButtonListener onDialogButtonListener) {
    mOnDialogButtonListener = onDialogButtonListener;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    mImageView = new ImageView(getActivity());
    mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
    mImageView.setAdjustViewBounds(true);
    ImageLoader.getInstance().loadImage(mImageModel.getThumbnailUrl(), this);
    builder.setView(mImageView);
    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (mOnDialogButtonListener != null) {
          mOnDialogButtonListener.onClickCancel(mImageModel);
        }
      }
    });
    builder.setPositiveButton("Share", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (mOnDialogButtonListener != null) {
          mOnDialogButtonListener.onClickShare(mImageModel);
        }
      }
    });

    Dialog dialog = builder.create();
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    return dialog;
  }

  @Override
  public void onLoadingStarted(String imageUrl, View view) {
  }

  @Override
  public void onLoadingFailed(String imageUrl, View view, FailReason failReason) {
  }

  @Override
  public void onLoadingComplete(String imageUrl, View view, Bitmap bitmap) {
    mImageView.setImageBitmap(bitmap);
    if (!imageUrl.equals(mImageModel.getOriginalUrl())) {
      ImageLoader.getInstance().loadImage(mImageModel.getOriginalUrl(), this);
    }
  }

  @Override
  public void onLoadingCancelled(String imageUrl, View view) {
  }
}
