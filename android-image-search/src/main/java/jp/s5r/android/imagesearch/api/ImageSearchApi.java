package jp.s5r.android.imagesearch.api;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import jp.s5r.android.imagesearch.model.ResponseModel;

import android.net.Uri;

public class ImageSearchApi {

  private static String API_VERSION = "1.0";
  private static String DEFAULT_COUNT = "8";

  private static String SCHEME = "https";
  private static String HOST = "ajax.googleapis.com";
  private static String PATH = "/ajax/services/search/images";
  private static String PARAM_VERSION = "v";
  private static String PARAM_COUNT = "rsz";
  private static String PARAM_QUERY = "q";
  private static String PARAM_START = "start";

  private final AsyncHttpClient mAsyncHttpClient;
  private final Gson mGson;

  private OnResponseListener mOnResponseListener;

  public interface OnResponseListener {
    void onResponse(ResponseModel response);
  }

  public ImageSearchApi() {
    mAsyncHttpClient = new AsyncHttpClient();
    mGson = new Gson();
  }

  public void setOnResponseListener(OnResponseListener onResponseListener) {
    mOnResponseListener = onResponseListener;
  }

  public void search(String query) {
    search(query, 0);
  }

  public void search(String query, int start) {
    String uri = buildUri(query, start);
    mAsyncHttpClient.get(uri, new AsyncHttpResponseHandler() {
      @Override
      public void onSuccess(String content) {
        ResponseModel response = mGson.fromJson(content, ResponseModel.class);
        if (response != null && mOnResponseListener != null) {
          mOnResponseListener.onResponse(response);
        }
      }
    });
  }

  private String buildUri(String query, int start) {
    Uri.Builder builder = new Uri.Builder()
      .scheme(SCHEME)
      .authority(HOST)
      .path(PATH);

    String encodedQuery = Uri.encode(query);

    builder.appendQueryParameter(PARAM_VERSION, API_VERSION);
    builder.appendQueryParameter(PARAM_COUNT, DEFAULT_COUNT);
    builder.appendQueryParameter(PARAM_QUERY, encodedQuery);
    if (start > 0) {
      builder.appendQueryParameter(PARAM_START, String.valueOf(start));
    }

    return builder.build().toString();
  }
}
