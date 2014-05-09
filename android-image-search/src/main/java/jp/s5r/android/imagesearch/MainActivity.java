package jp.s5r.android.imagesearch;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

public class MainActivity extends BaseActivity implements SearchView.OnQueryTextListener {

  private ImageGridFragment mFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (savedInstanceState == null) {
      mFragment = new ImageGridFragment();
      addFragment(mFragment);
    }

    Intent intent = getIntent();
    if (intent != null) {
      if (Intent.ACTION_PICK.equals(intent.getAction()) || Intent.ACTION_GET_CONTENT.equals(intent.getAction())) {
        if (mFragment != null) {
          mFragment.setIntentMode(true);
        }
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
    SearchView searchView = null;
    if (searchItem != null) {
      searchView = (SearchView) searchItem.getActionView();
    }
    if (searchView != null) {
      searchView.setOnQueryTextListener(this);
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
}
