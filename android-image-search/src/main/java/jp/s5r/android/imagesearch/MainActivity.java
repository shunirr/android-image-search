package jp.s5r.android.imagesearch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import java.io.File;

public class MainActivity extends BaseActivity implements SearchView.OnQueryTextListener {

  private ImageGridFragment mFragment;
  private SearchView mSearchView;
  private boolean mIsSearchViewExpanded;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (savedInstanceState == null) {
      mFragment = new ImageGridFragment();
      addFragment(mFragment);
    }

    Intent intent = getIntent();
    if (intent != null && mFragment != null) {
      String a = intent.getAction();
      if (Intent.ACTION_PICK.equals(a) || Intent.ACTION_GET_CONTENT.equals(a)) {
        mFragment.setIntentPickerMode(true);
      } else if (MediaStore.ACTION_IMAGE_CAPTURE.equals(a)) {
        Uri saveFileUri = intent.getParcelableExtra(MediaStore.EXTRA_OUTPUT);
        File saveFile = null;
        if (saveFileUri != null) {
          saveFile = new File(saveFileUri.getPath());
        }
        mFragment.setIntentCaptureMode(true, saveFile);
      }
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.menu_config:
      popToTop();
      pushFragment(new ConfigFragment());
      return true;

    default:
      popToTop();
      return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);

    MenuItem searchItem = menu.findItem(R.id.menu_search);
    if (searchItem != null) {
      mSearchView = (SearchView) searchItem.getActionView();
    }
    if (mSearchView != null) {
      mSearchView.setQueryHint("");
      mSearchView.setOnQueryTextListener(this);
      mSearchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
          if (hasFocus) {
            mIsSearchViewExpanded = true;
          }
        }
      });
    }

    return true;
  }

  @Override
  public boolean onQueryTextChange(String newText) {
    if (mFragment != null) {
      return mFragment.onQueryTextChange(newText);
    }
    return false;
  }

  @Override
  public boolean onQueryTextSubmit(final String query) {
    if (popToTop()) {
      new Handler(getMainLooper()).postDelayed(new Runnable() {
        @Override
        public void run() {
          if (mFragment != null) {
            mFragment.onQueryTextSubmit(query);
          }
        }
      }, 100);
    } else {
      if (mFragment != null) {
        return mFragment.onQueryTextSubmit(query);
      }
    }
    return false;
  }

  @Override
  public void onBackPressed() {
    if (mSearchView != null && mIsSearchViewExpanded) {
      mSearchView.onActionViewCollapsed();
      mIsSearchViewExpanded = false;
    } else {
      super.onBackPressed();
    }
  }
}
