package jp.s5r.android.imagesearch;

import butterknife.ButterKnife;
import butterknife.InjectView;
import com.nostra13.universalimageloader.core.ImageLoader;
import jp.s5r.android.imagesearch.model.ResultModel;
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

  private List<ResultModel> mResultList;
  private OnItemClickListener mOnItemClickListener;

  public interface OnItemClickListener {
    void onItemClick(ResultModel item);
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

  public void addAll(List<ResultModel> results) {
    if (mResultList == null) {
      mResultList = new ArrayList<ResultModel>();
    }
    mResultList.addAll(results);
  }

  public void clear() {
    if (mResultList == null) {
      mResultList = new ArrayList<ResultModel>();
    }
    mResultList.clear();
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

    ViewGroup.LayoutParams params = view.getLayoutParams();
    params.width = mItemWidth;
    params.height = mItemWidth;
    view.setLayoutParams(params);

    final ResultModel item = getItem(position);

    Object o = holder.image.getTag();
    if (o != null && o instanceof String) {
      String viewUri = (String) o;
      if (!viewUri.equals(item.getTbUrl())) {
        holder.image.setImageBitmap(null);
      }
    }
    holder.image.setTag(item.getTbUrl());
    ImageLoader.getInstance().displayImage(item.getTbUrl(), holder.image);
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
