package jp.s5r.android.imagesearch.dialog;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import jp.s5r.android.imagesearch.R;
import jp.s5r.android.imagesearch.model.ImageModel;
import jp.s5r.android.imagesearch.util.DisplayUtil;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

public class ImagePreviewDialogFragment extends DialogFragment implements ImageLoadingListener {

  public interface OnDialogButtonListener {
    void onClickShare(ImageModel model);
    void onClickCancel(ImageModel model);
  }

  private final ImageModel mImageModel;

  private ImageView mImageView;
  private OnDialogButtonListener mOnDialogButtonListener;

  public ImagePreviewDialogFragment(ImageModel imageModel) {
    mImageModel = imageModel;
  }

  public void setOnDialogButtonListener(OnDialogButtonListener onDialogButtonListener) {
    mOnDialogButtonListener = onDialogButtonListener;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    Dialog dialog = getDialog();
    WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
    int displayWidth  = DisplayUtil.getDisplayWidth(getActivity().getWindowManager());
    int displayHeight = DisplayUtil.getDisplayHeight(getActivity().getWindowManager());
    int buttonHeight = DisplayUtil.convertDpToPixel(getActivity(), 52);

    float scale = 0.9f;
    while (true) {
      lp.width = (int) (displayWidth * scale);
      lp.height = (int) (displayWidth * scale * mImageModel.getAspectRatio()) + buttonHeight;
      if (lp.height < (displayHeight * 0.9)) {
        break;
      }
      scale -= 0.05f;
    }

    dialog.getWindow().setAttributes(lp);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Dialog dialog = new Dialog(getActivity());
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setContentView(R.layout.preview_dialog);
    mImageView = (ImageView) dialog.findViewById(R.id.preview_dialog_image);
    mImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    mImageView.setAdjustViewBounds(true);
    ImageLoader.getInstance().loadImage(mImageModel.getThumbnailUrl(), this);
    Button leftButton = (Button) dialog.findViewById(R.id.preview_dialog_left_button);
    leftButton.setText("Cancel");
    leftButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dismiss();
        if (mOnDialogButtonListener != null) {
          mOnDialogButtonListener.onClickCancel(mImageModel);
        }
      }
    });
    Button rightButton = (Button) dialog.findViewById(R.id.preview_dialog_right_button);
    rightButton.setText("Share");
    rightButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dismiss();
        if (mOnDialogButtonListener != null) {
          mOnDialogButtonListener.onClickShare(mImageModel);
        }
      }
    });
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
    if (!isAdded()) {
      return;
    }
    mImageView.setImageBitmap(bitmap);
    if (!imageUrl.equals(mImageModel.getOriginalUrl())) {
      ImageLoader.getInstance().loadImage(mImageModel.getOriginalUrl(), this);
    }
  }

  @Override
  public void onLoadingCancelled(String imageUrl, View view) {
  }
}
