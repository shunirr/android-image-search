package jp.s5r.android.imagesearch;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import jp.s5r.android.imagesearch.api.ImageSearchApi;
import jp.s5r.android.imagesearch.model.CursorModel;
import jp.s5r.android.imagesearch.model.ResponseDataModel;
import jp.s5r.android.imagesearch.model.ResponseModel;
import jp.s5r.android.imagesearch.model.ResultModel;
import jp.s5r.android.imagesearch.util.ImageUtil;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.SearchView;

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

  @InjectView(R.id.grid)
  GridView mGridView;

  private boolean mIsLoading;
  private boolean mHasNext;
  private String mCurrentQuery;
  private int mNextStart;

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
    mAdapter.clear();
    mCurrentQuery = query;
    mNextStart = 0;
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
  public boolean onQueryTextChange(String newText) {
    return false;
  }

  @Override
  public void onItemClick(ResultModel result) {
    asyncDownloadImage(result.getUrl());
  }

  private void asyncDownloadImage(String uri) {
    ImageLoader.getInstance().loadImage(
      uri,
      new SimpleImageLoadingListener() {
        @Override
        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
          onDownloadComplete(bitmap);
        }
      });
  }

  private void onDownloadComplete(Bitmap bitmap) {
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
}
