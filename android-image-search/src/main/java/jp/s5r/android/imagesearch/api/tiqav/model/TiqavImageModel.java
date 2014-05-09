package jp.s5r.android.imagesearch.api.tiqav.model;

import lombok.Data;

@Data
public class TiqavImageModel {
  private String id;
  private String ext;
  private int width;
  private int height;
  private String source_url;

  public String getThumbnailUrl() {
    if (id != null) {
      return "http://img.tiqav.com/" + id + ".th.jpg";
    }
    return null;
  }
}
