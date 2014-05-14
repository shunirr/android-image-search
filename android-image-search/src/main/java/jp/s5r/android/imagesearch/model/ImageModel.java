package jp.s5r.android.imagesearch.model;

import jp.s5r.android.imagesearch.api.googleimage.model.ResultModel;
import jp.s5r.android.imagesearch.api.tiqav.model.TiqavImageModel;

public class ImageModel {
  private final String mThumbnailUrl;
  private final String mOriginalUrl;
  private final float mAspectRatio;

  public ImageModel(TiqavImageModel tiqavImage) {
    mThumbnailUrl = tiqavImage.getThumbnailUrl();
    mOriginalUrl = tiqavImage.getThumbnailUrl();
    mAspectRatio = (float) tiqavImage.getHeight() / (float) tiqavImage.getWidth();
  }

  public ImageModel(ResultModel googleImage) {
    mThumbnailUrl = googleImage.getTbUrl();
    mOriginalUrl = googleImage.getUnescapedUrl();
    mAspectRatio = (float) googleImage.getHeight() / (float) googleImage.getWidth();
  }

  public String getThumbnailUrl() {
    return mThumbnailUrl;
  }

  public String getOriginalUrl() {
    return mOriginalUrl;
  }

  public float getAspectRatio() {
    return mAspectRatio;
  }
}
