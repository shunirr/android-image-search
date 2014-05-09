package jp.s5r.android.imagesearch;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import jp.s5r.android.imagesearch.api.googleimage.GoogleImageSearchApi;
import jp.s5r.android.imagesearch.api.googleimage.model.CursorModel;
import jp.s5r.android.imagesearch.api.googleimage.model.ResponseDataModel;
import jp.s5r.android.imagesearch.api.googleimage.model.ResponseModel;
import jp.s5r.android.imagesearch.api.tiqav.TiqavApi;
import jp.s5r.android.imagesearch.api.tiqav.model.TiqavImageModel;
import jp.s5r.android.imagesearch.model.ImageModel;
import jp.s5r.android.imagesearch.util.Config;
import jp.s5r.android.imagesearch.util.ImageUtil;
import jp.s5r.android.imagesearch.util.IntentUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ImageGridFragment
  extends BaseFragment
  implements SearchView.OnQueryTextListener,
             GoogleImageSearchApi.OnGoogleImageResponseListener,
             ImageGridAdapter.OnItemClickListener,
             AbsListView.OnScrollListener,
             TiqavApi.OnTiqavResponseListener {

  private GoogleImageSearchApi mGoogleImageSearchApi;
  private TiqavApi mTiqavApi;

  private ImageGridAdapter mAdapter;
  private boolean mIsIntentMode;
  private File mCacheDir;

  private boolean mIsLoading;
  private boolean mHasNext;
  private String mCurrentQuery;
  private int mNextStart;
  private boolean mIsLoadTiqav;

  private ProgressDialogFragment mProgressDialog;

  @InjectView(R.id.grid)
  GridView mGridView;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_main, container, false);
    ButterKnife.inject(this, rootView);
    return rootView;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    ButterKnife.reset(this);
  }

  private void initCacheDir() {
    File cacheDir = new File(Environment.getExternalStorageDirectory(), "Pictures/ImageSearch/");
    if (!cacheDir.exists()) {
      cacheDir.mkdirs();
    }
    mCacheDir = cacheDir;
  }

  @Override
  public void onStart() {
    super.onStart();

    mNextStart = 0;
    mIsLoadTiqav = false;
    mHasNext = false;

    mAdapter = new ImageGridAdapter(getActivity());
    mAdapter.setOnItemClickListener(this);
    mGridView.setAdapter(mAdapter);
    mGridView.setOnScrollListener(this);
    mGoogleImageSearchApi = new GoogleImageSearchApi();
    mGoogleImageSearchApi.setOnGoogleImageResponseListener(this);

    mTiqavApi = new TiqavApi();
    mTiqavApi.setOnTiqavResponseListener(this);

    initCacheDir();
  }

  @Override
  public void onStop() {
    mAdapter = null;
    if (mGridView != null) {
      mGridView.setAdapter(null);
    }
    if (mGoogleImageSearchApi != null) {
      mGoogleImageSearchApi.setOnGoogleImageResponseListener(null);
      mGoogleImageSearchApi = null;
    }
    if (mTiqavApi != null) {
      mTiqavApi.setOnTiqavResponseListener(null);
      mTiqavApi = null;
    }

    super.onStop();
  }

  public void setIntentMode(boolean value) {
    mIsIntentMode = value;
  }

  @Override
  public boolean onQueryTextSubmit(String query) {
    hideSoftKeyboard();

    mAdapter.clear();
    mCurrentQuery = query;
    mNextStart = 0;
    mIsLoadTiqav = false;

    mProgressDialog = new ProgressDialogFragment();
    mProgressDialog.show(getFragmentManager(), "dialog");

    loadItems();
    return true;
  }

  private synchronized void loadItems() {
    mIsLoading = true;
    if (Config.useTiqav(getActivity()) && !mIsLoadTiqav) {
      mTiqavApi.search(mCurrentQuery);
    } else {
      mGoogleImageSearchApi.search(mCurrentQuery, mNextStart);
    }
  }

  @Override
  public void onGoogleImageResponse(ResponseModel response) {
    mIsLoading = false;
    if (mProgressDialog != null) {
      mProgressDialog.dismiss();
      mProgressDialog = null;
      hideSoftKeyboard();
    }

    ResponseDataModel responseData = response.getResponseData();
    if (responseData != null) {
      CursorModel cursor = responseData.getCursor();
      if ((cursor.getPages().size() * 8) > mNextStart) {
        mHasNext = true;
        mNextStart += 8;
      } else {
        mHasNext = false;
      }

      if (mAdapter != null) {
        mAdapter.addGoogleImages(responseData.getResults());
        mAdapter.notifyDataSetChanged();
      }
    } else {
      mHasNext = false;
    }
  }

  @Override
  public void onGoogleImageFailure() {
    onRequestFailure();
  }

  @Override
  public boolean onQueryTextChange(String newText) {
    return false;
  }

  @Override
  public void onItemClick(ImageModel result) {
    asyncDownloadImage(result.getOriginalUrl());
  }

  private void asyncDownloadImage(String uri) {
    mProgressDialog = new ProgressDialogFragment();
    mProgressDialog.show(getFragmentManager(), "dialog");

    ImageLoader.getInstance().loadImage(
      uri,
      new SimpleImageLoadingListener() {
        @Override
        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
          onDownloadComplete(bitmap);
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
          onDownloadFailed(failReason.getCause().getMessage());
        }
      });
  }

  private void onDownloadFailed(String message) {
    if (mProgressDialog != null) {
      mProgressDialog.dismiss();
      mProgressDialog = null;
    }

    Toast.makeText(getActivity(), "Failed to download image.", Toast.LENGTH_LONG).show();
  }

  private void onDownloadComplete(Bitmap bitmap) {
    if (mProgressDialog != null) {
      mProgressDialog.dismiss();
      mProgressDialog = null;
    }

    File path = new File(mCacheDir, System.currentTimeMillis() + ".jpg");
    try {
      ImageUtil.saveImage(path, bitmap);
      Uri uri = Uri.fromFile(path);
      if (Config.saveGallery(getActivity())) {
        uri = ImageUtil.addGarally(getActivity(), path);
      }

      if (mIsIntentMode) {
        Intent intent = new Intent();
        intent.setData(uri);
        getActivity().setResult(Activity.RESULT_OK, intent);
      } else {
        IntentUtil.shareImage(getActivity(), uri);
      }
    } catch (IOException ignored) {}

    if (mIsIntentMode) {
      getActivity().finish();
    }
  }

  @Override
  public void onScrollStateChanged(AbsListView view, int scrollState) {
  }

  @Override
  public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    boolean isLastItemVisible = totalItemCount == firstVisibleItem + visibleItemCount;
    if (isLastItemVisible && !mIsLoading && mHasNext) {
      loadItems();
    }
  }

  private void hideSoftKeyboard() {
    new Handler(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        InputMethodManager inputMethodManager =
          (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        mGridView.requestFocus();
      }
    });
  }

  @Override
  public void onTiqavResponse(List<TiqavImageModel> response) {
    mIsLoading = false;
    if (mProgressDialog != null) {
      mProgressDialog.dismiss();
      mProgressDialog = null;
      hideSoftKeyboard();
    }

    mIsLoadTiqav = true;
    mHasNext = true;
    if (mAdapter != null) {
      mAdapter.addTiqavImages(response);
      mAdapter.notifyDataSetChanged();
    }
  }

  @Override
  public void onTiqavFailure() {
    mIsLoadTiqav = true;
    loadItems();
  }

  private void onRequestFailure() {
    mIsLoading = false;
    if (mProgressDialog != null) {
      mProgressDialog.dismiss();
      mProgressDialog = null;
      hideSoftKeyboard();
    }
    mHasNext = false;
  }
}
