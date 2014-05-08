package jp.s5r.android.imagesearch.model;

import lombok.Data;

import java.util.List;

@Data
public class ResponseDataModel {
  private List<ResultModel> results;
  private CursorModel cursor;
}
