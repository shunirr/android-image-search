package jp.s5r.android.imagesearch.api.tiqav;

import com.bugsense.trace.BugSenseHandler;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpResponseHandler;
import jp.s5r.android.imagesearch.api.BaseApi;
import jp.s5r.android.imagesearch.api.tiqav.model.TiqavImageModel;

import android.net.Uri;
import android.util.Log;

import java.util.List;

public class TiqavApi extends BaseApi {

  private OnTiqavResponseListener mOnTiqavResponseListener;

  public interface OnTiqavResponseListener {
    void onTiqavResponse(List<TiqavImageModel> response);

    void onTiqavFailure();
  }

  public TiqavApi() {
    super();
  }

  public void setOnTiqavResponseListener(OnTiqavResponseListener onTiqavResponseListener) {
    mOnTiqavResponseListener = onTiqavResponseListener;
  }

  @Override
  public void search(String query) {
    String uri = "http://api.tiqav.com/search.json?q=" + Uri.encode(query);
    getAsyncHttpClient().get(uri, new AsyncHttpResponseHandler() {
      @Override
      public void onStart() {
        super.onStart();
        Log.d("ImageSearch", "Request: " + getRequestURI().toString());
      }

      @Override
      public void onSuccess(String content) {
        super.onSuccess(content);
        List<TiqavImageModel> response = getGson().fromJson(content, new TypeToken<List<TiqavImageModel>>(){}.getType());
        if (response != null) {
          if (mOnTiqavResponseListener != null) {
            mOnTiqavResponseListener.onTiqavResponse(response);
          }
        } else {
          BugSenseHandler.sendException(new Exception("TiqavResponse is null."));
        }
      }

      @Override
      public void onFailure(Throwable error) {
        super.onFailure(error);
        if (mOnTiqavResponseListener != null) {
          mOnTiqavResponseListener.onTiqavFailure();
          if (error instanceof Exception) {
            BugSenseHandler.sendException((Exception) error);
          } else {
            BugSenseHandler.sendException(new Exception(error));
          }
        }
      }
    });
  }
}
