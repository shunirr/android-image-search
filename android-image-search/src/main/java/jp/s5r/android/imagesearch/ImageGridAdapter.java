package jp.s5r.android.imagesearch;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.nostra13.universalimageloader.core.ImageLoader;
import jp.s5r.android.imagesearch.api.googleimage.model.ResultModel;
import jp.s5r.android.imagesearch.api.model.ImageModel;
import jp.s5r.android.imagesearch.api.tiqav.model.TiqavImageModel;
import jp.s5r.android.imagesearch.util.DisplayUtil;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class ImageGridAdapter extends BaseAdapter {

  private final LayoutInflater mInflater;
  private final int mItemWidth;

  private List<ImageModel> mImageList;
  private OnItemClickListener mOnItemClickListener;

  public interface OnItemClickListener {
    void onItemClick(ImageModel item);
  }

  public ImageGridAdapter(Activity context) {
    mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    WindowManager wm = context.getWindowManager();
    int displayWidth = DisplayUtil.getDisplayWidth(wm);
    mItemWidth = displayWidth / 3;
  }

  public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
    mOnItemClickListener = onItemClickListener;
  }

  public void addGoogleImages(List<ResultModel> results) {
    if (mImageList == null) {
      mImageList = new ArrayList<ImageModel>();
    }
    for (ResultModel result : results) {
      mImageList.add(new ImageModel(result));
    }
  }

  public void addTiqavImages(List<TiqavImageModel> results) {
    if (mImageList == null) {
      mImageList = new ArrayList<ImageModel>();
    }
    for (TiqavImageModel result : results) {
      mImageList.add(new ImageModel(result));
    }
  }

  public void clear() {
    if (mImageList == null) {
      mImageList = new ArrayList<ImageModel>();
    }
    mImageList.clear();
  }

  @Override
  public int getCount() {
    if (mImageList != null) {
      return mImageList.size();
    }
    return 0;
  }

  @Override
  public ImageModel getItem(int position) {
    if (mImageList != null) {
      return mImageList.get(position);
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

    ViewGroup.LayoutParams params = view.getLayoutParams();
    params.width = mItemWidth;
    params.height = mItemWidth;
    view.setLayoutParams(params);

    final ImageModel item = getItem(position);

    Object o = holder.image.getTag();
    if (o != null && o instanceof String) {
      String viewUri = (String) o;
      if (!viewUri.equals(item.getThumbnailUrl())) {
        holder.image.setImageBitmap(null);
      }
    }
    holder.image.setTag(item.getThumbnailUrl());
    ImageLoader.getInstance().displayImage(item.getThumbnailUrl(), holder.image);
    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mOnItemClickListener != null) {
          mOnItemClickListener.onItemClick(item);
        }
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
