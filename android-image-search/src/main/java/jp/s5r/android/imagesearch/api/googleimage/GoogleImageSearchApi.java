package jp.s5r.android.imagesearch.api.googleimage;

import com.loopj.android.http.AsyncHttpResponseHandler;
import jp.s5r.android.imagesearch.api.BaseApi;
import jp.s5r.android.imagesearch.api.googleimage.model.ResponseModel;

import android.net.Uri;
import android.util.Log;

public class GoogleImageSearchApi extends BaseApi {

  private static final String API_VERSION = "1.0";
  private static final String DEFAULT_COUNT = "8";

  private static final String SCHEME = "https";
  private static final String HOST = "ajax.googleapis.com";
  private static final String PATH = "/ajax/services/search/images";
  private static final String PARAM_VERSION = "v";
  private static final String PARAM_COUNT = "rsz";
  private static final String PARAM_QUERY = "q";
  private static final String PARAM_START = "start";

  private OnGoogleImageResponseListener mOnGoogleImageResponseListener;

  public interface OnGoogleImageResponseListener {
    void onGoogleImageResponse(ResponseModel response);

    void onGoogleImageFailure();
  }

  public GoogleImageSearchApi() {
    super();
  }

  public void setOnGoogleImageResponseListener(OnGoogleImageResponseListener onGoogleImageResponseListener) {
    mOnGoogleImageResponseListener = onGoogleImageResponseListener;
  }

  @Override
  public void search(String query) {
    search(query, 0);
  }

  public void search(String query, int start) {
    String uri = buildUri(query, start);
    getAsyncHttpClient().get(uri, new AsyncHttpResponseHandler() {

      @Override
      public void onStart() {
        super.onStart();
        Log.d("ImageSearch", "Request: " + getRequestURI().toString());
      }

      @Override
      public void onSuccess(String content) {
        super.onSuccess(content);
        ResponseModel response = getGson().fromJson(content, ResponseModel.class);
        if (response != null && mOnGoogleImageResponseListener != null) {
          mOnGoogleImageResponseListener.onGoogleImageResponse(response);
        }
      }

      @Override
      public void onFailure(Throwable error) {
        super.onFailure(error);
        if (mOnGoogleImageResponseListener != null) {
          mOnGoogleImageResponseListener.onGoogleImageFailure();
        }
      }
    });
  }

  private String buildUri(String query, int start) {
    Uri.Builder builder = new Uri.Builder()
      .scheme(SCHEME)
      .authority(HOST)
      .path(PATH);

    builder.appendQueryParameter(PARAM_VERSION, API_VERSION);
    builder.appendQueryParameter(PARAM_COUNT, DEFAULT_COUNT);
    builder.appendQueryParameter(PARAM_QUERY, query.replaceAll("[ 　]+", "+"));
    if (start > 0) {
      builder.appendQueryParameter(PARAM_START, String.valueOf(start));
    }

    return builder.build().toString();
  }
}
