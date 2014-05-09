package jp.s5r.android.imagesearch.api.googleimage.model;

import lombok.Data;

@Data
public class ResultModel {
  private String GsearchResultClass;
  private int width;
  private int height;
  private String imageId;
  private int tbWidth;
  private int tbHeight;
  private String unescapedUrl;
  private String url;
  private String visibleUrl;
  private String title;
  private String titleNoFormatting;
  private String originalContextUrl;
  private String content;
  private String contentNoFormatting;
  private String tbUrl;
}
