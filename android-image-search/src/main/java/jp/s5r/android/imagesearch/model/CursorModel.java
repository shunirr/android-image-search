package jp.s5r.android.imagesearch.model;

import lombok.Data;

import java.util.List;

@Data
public class CursorModel {
  private String resultCount;
  private List<PageModel> pages;
  private String estimatedResultCount;
  private int currentPageIndex;
  private String moreResultsUrl;
  private String searchResultTime;
}
