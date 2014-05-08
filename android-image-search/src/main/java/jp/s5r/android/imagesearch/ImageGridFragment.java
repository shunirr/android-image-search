package jp.s5r.android.imagesearch;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import jp.s5r.android.imagesearch.model.ResponseModel;
import jp.s5r.android.imagesearch.model.ResultModel;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageGridFragment extends BaseFragment implements SearchView.OnQueryTextListener {

  private static String API = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&rsz=8&q=";

  private ImageAdapter mAdapter;
  private AsyncHttpClient mAsyncHttpClient;
  private Gson mGson;
  private boolean mIsIntentMode;

  @InjectView(R.id.grid)
  GridView mGridView;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
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

    mAdapter = new ImageAdapter(getActivity());
    mGridView.setAdapter(mAdapter);
    mAsyncHttpClient = new AsyncHttpClient();
    mGson = new Gson();
  }

  @Override
  public void onDestroy() {
    mAdapter = null;
    if (mGridView != null) {
      mGridView.setAdapter(null);
    }

    super.onDestroy();
  }

  public void setIntentMode(boolean value) {
    mIsIntentMode = value;
  }

  @Override
  public boolean onQueryTextSubmit(String query) {
    String param = Uri.encode(query);
    mAsyncHttpClient.get(API + param, new AsyncHttpResponseHandler() {
      @Override
      public void onSuccess(String content) {
        ResponseModel response = mGson.fromJson(content, ResponseModel.class);
        if (response != null) {
          onResponse(response);
        }
      }
    });
    return false;
  }

  private void onResponse(ResponseModel response) {
    if (mAdapter != null) {
      mAdapter.addAll(response.getResponseData().getResults());
      mAdapter.notifyDataSetChanged();
    }
  }

  @Override
  public boolean onQueryTextChange(String newText) {
    return false;
  }

  private void onItemClick(ResultModel result) {
    ImageLoader.getInstance().loadImage(
      result.getUrl(),
      new SimpleImageLoadingListener() {
        @Override
        public void onLoadingComplete(String s, View view, Bitmap bitmap) {
          File path = new File("/sdcard/hoge.jpg");
          try {
            saveImage(path, bitmap);
            Uri uri = addGarally(path);
            if (mIsIntentMode) {
              Intent intent = new Intent();
              intent.setData(uri);
              getActivity().setResult(Activity.RESULT_OK, intent);
              getActivity().finish();
            }
          } catch (IOException ignored) {}
        }
      });
  }

  private void saveImage(File path, Bitmap bmp) throws IOException {
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(path);
      bmp.compress(Bitmap.CompressFormat.JPEG, 95, out);
      out.flush();
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }

  private Uri addGarally(File path) throws IOException {
    ContentValues values = new ContentValues();
    ContentResolver contentResolver = getActivity().getContentResolver();
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
    values.put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000);
    values.put(MediaStore.Images.Media.SIZE, path.length());
    values.put(MediaStore.Images.Media.TITLE, path.getName());
    values.put(MediaStore.Images.Media.DATA, path.getPath());
    return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
  }

  class ImageAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;

    private List<ResultModel> mResultList;

    private ImageAdapter(Context context) {
      mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addAll(List<ResultModel> results) {
      if (mResultList == null) {
        mResultList = new ArrayList<ResultModel>();
      }
      mResultList.addAll(results);
    }

    @Override
    public int getCount() {
      if (mResultList != null) {
        return mResultList.size();
      }
      return 0;
    }

    @Override
    public ResultModel getItem(int position) {
      if (mResultList != null) {
        return mResultList.get(position);
      }
      return null;
    }

    @Override
    public long getItemId(int position) {
      return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view = convertView;
      ViewHolder holder;
      if (view != null) {
        holder = (ViewHolder) view.getTag();
      } else {
        view = mInflater.inflate(R.layout.grid_item, parent, false);
        holder = new ViewHolder(view);
        view.setTag(holder);
      }

      final ResultModel item = getItem(position);
      ImageLoader.getInstance().displayImage(item.getTbUrl(), holder.image);
      view.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          onItemClick(item);
        }
      });

      return view;
    }

    class ViewHolder {
      @InjectView(R.id.grid_item_image)
      ImageView image;

      public ViewHolder(View view) {
        ButterKnife.inject(this, view);
      }
    }
  }
}
