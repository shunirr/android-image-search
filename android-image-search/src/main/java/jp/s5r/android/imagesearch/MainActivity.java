package jp.s5r.android.imagesearch;

import android.content.Intent;
import android.os.Bundle;
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
      getFragmentManager().beginTransaction()
        .add(R.id.container, mFragment)
        .commit();
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
  public boolean onQueryTextSubmit(String query) {
    if (mFragment != null) {
      return mFragment.onQueryTextSubmit(query);
    }
    return false;
  }
}
