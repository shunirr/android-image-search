package jp.s5r.android.imagesearch.api.googleimage.model;

import lombok.Data;

@Data
public class ResponseModel {
  private ResponseDataModel responseData;
  private String responseDetails;
  private int responseStatus;
}
