package jp.s5r.android.imagesearch;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import jp.s5r.android.imagesearch.api.ImageSearchApi;
import jp.s5r.android.imagesearch.model.CursorModel;
import jp.s5r.android.imagesearch.model.ResponseDataModel;
import jp.s5r.android.imagesearch.model.ResponseModel;
import jp.s5r.android.imagesearch.model.ResultModel;
import jp.s5r.android.imagesearch.util.ImageUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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

public class ImageGridFragment
  extends BaseFragment
  implements SearchView.OnQueryTextListener,
             ImageSearchApi.OnResponseListener,
             ImageGridAdapter.OnItemClickListener,
             AbsListView.OnScrollListener {

  private ImageSearchApi mImageSearchApi;
  private ImageGridAdapter mAdapter;
  private boolean mIsIntentMode;
  private File mCacheDir;

  private boolean mIsLoading;
  private boolean mHasNext;
  private String mCurrentQuery;
  private int mNextStart;

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

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    mAdapter = new ImageGridAdapter(getActivity());
    mAdapter.setOnItemClickListener(this);
    mGridView.setAdapter(mAdapter);
    mGridView.setOnScrollListener(this);
    mImageSearchApi = new ImageSearchApi();
    mImageSearchApi.setOnResponseListener(this);

    initCacheDir();
  }

  private void initCacheDir() {
    File cacheDir = getActivity().getExternalCacheDir();
    if (!cacheDir.exists()) {
      cacheDir.mkdirs();
    }
    mCacheDir = cacheDir;
  }

  @Override
  public void onDestroy() {
    mAdapter = null;
    if (mGridView != null) {
      mGridView.setAdapter(null);
    }
    if (mImageSearchApi != null) {
      mImageSearchApi.setOnResponseListener(null);
      mImageSearchApi = null;
    }

    super.onDestroy();
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

    mProgressDialog = new ProgressDialogFragment();
    mProgressDialog.show(getFragmentManager(), "dialog");

    loadItems();
    return true;
  }

  private synchronized void loadItems() {
    mIsLoading = true;
    mImageSearchApi.search(mCurrentQuery, mNextStart);
  }

  @Override
  public void onResponse(ResponseModel response) {
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
        mAdapter.addAll(responseData.getResults());
        mAdapter.notifyDataSetChanged();
      }
    } else {
      mHasNext = false;
    }
  }

  @Override
  public void onFailure() {
    mIsLoading = false;
    if (mProgressDialog != null) {
      mProgressDialog.dismiss();
      mProgressDialog = null;
      hideSoftKeyboard();
    }
    mHasNext = false;
  }

  @Override
  public boolean onQueryTextChange(String newText) {
    return false;
  }

  @Override
  public void onItemClick(ResultModel result) {
    if (mIsIntentMode) {
      asyncDownloadImage(result.getUnescapedUrl());
    }
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
      Uri uri = ImageUtil.addGarally(getActivity(), path);
      if (mIsIntentMode) {
        Intent intent = new Intent();
        intent.setData(uri);
        getActivity().setResult(Activity.RESULT_OK, intent);
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
}
