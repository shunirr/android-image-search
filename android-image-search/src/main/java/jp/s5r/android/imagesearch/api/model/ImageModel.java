package jp.s5r.android.imagesearch.api.model;

import jp.s5r.android.imagesearch.api.googleimage.model.ResultModel;
import jp.s5r.android.imagesearch.api.tiqav.model.TiqavImageModel;

public class ImageModel {
  private final String mThumbnailUrl;
  private final String mOriginalUrl;

  public ImageModel(TiqavImageModel tiqavImage) {
    mThumbnailUrl = tiqavImage.getThumbnailUrl();
    mOriginalUrl = tiqavImage.getThumbnailUrl();
  }

  public ImageModel(ResultModel googleImage) {
    mThumbnailUrl = googleImage.getTbUrl();
    mOriginalUrl = googleImage.getUnescapedUrl();
  }

  public String getThumbnailUrl() {
    return mThumbnailUrl;
  }

  public String getOriginalUrl() {
    return mOriginalUrl;
  }
}
