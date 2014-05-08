package jp.s5r.android.imagesearch.model;

import lombok.Data;

@Data
public class ResponseModel {
  private ResponseDataModel responseData;
  private String responseDetails;
  private int responseStatus;
}
