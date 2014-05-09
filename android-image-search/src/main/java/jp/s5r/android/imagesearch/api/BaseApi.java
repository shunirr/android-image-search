package jp.s5r.android.imagesearch.api;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;

public abstract class BaseApi {
  private final AsyncHttpClient mAsyncHttpClient;
  private final Gson mGson;

  protected BaseApi() {
    mAsyncHttpClient = new AsyncHttpClient();
    mGson = new Gson();
  }

  protected AsyncHttpClient getAsyncHttpClient() {
    return mAsyncHttpClient;
  }

  protected Gson getGson() {
    return mGson;
  }

  public abstract void search(String query);
}
