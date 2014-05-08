package jp.s5r.android.imagesearch;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import jp.s5r.android.imagesearch.api.ImageSearchApi;
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
import android.widget.GridView;
import android.widget.SearchView;

import java.io.File;
import java.io.IOException;

public class ImageGridFragment
  extends BaseFragment
  implements SearchView.OnQueryTextListener,
             ImageSearchApi.OnResponseListener,
             ImageGridAdapter.OnItemClickListener {

  private ImageSearchApi mImageSearchApi;
  private ImageGridAdapter mAdapter;
  private boolean mIsIntentMode;
  private File mCacheDir;

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
    mImageSearchApi.search(query);
    return false;
  }

  @Override
  public void onResponse(ResponseModel response) {
    if (mAdapter != null) {
      mAdapter.addAll(response.getResponseData().getResults());
      mAdapter.notifyDataSetChanged();
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
}
