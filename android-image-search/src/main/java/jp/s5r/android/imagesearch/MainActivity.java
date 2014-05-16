package jp.s5r.android.imagesearch;

import butterknife.ButterKnife;
import butterknife.InjectView;
import jp.s5r.android.imagesearch.api.googlesuggest.GoogleSuggestApi;
import jp.s5r.android.imagesearch.util.Config;
import jp.s5r.android.imagesearch.util.FileUtil;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.List;

public class MainActivity
  extends BaseActivity
  implements SearchView.OnQueryTextListener,
             GoogleSuggestApi.OnGoogleSuggestResponseListener,
             AdapterView.OnItemClickListener {

  private ImageGridFragment mFragment;
  private SearchView mSearchView;
  private boolean mIsSearchViewExpanded;

  @InjectView(R.id.suggestion)
  ListView mSuggestionList;
  private GoogleSuggestApi mGoogleSuggestApi;
  private String mSuggestionQuery;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ButterKnife.inject(this);

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
        mFragment.setIntentCaptureMode(true, FileUtil.getRealFile(this, saveFileUri));
      }
    }

    mGoogleSuggestApi = new GoogleSuggestApi();
    mGoogleSuggestApi.setOnGoogleSuggestResponseListener(this);
  }

  @Override
  protected void onDestroy() {
    if (mGoogleSuggestApi != null) {
      mGoogleSuggestApi.setOnGoogleSuggestResponseListener(null);
      mGoogleSuggestApi = null;
    }

    ButterKnife.reset(this);
    super.onDestroy();
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
      mSearchView.setQueryHint(getString(R.string.searchview_hint));
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
    if (!newText.equals(mSuggestionQuery) && !TextUtils.isEmpty(newText)) {
      if (Config.useSuggestion(this)) {
        mGoogleSuggestApi.suggest(newText);
      }
    }
    if (mFragment != null) {
      return mFragment.onQueryTextChange(newText);
    }
    return false;
  }

  @Override
  public boolean onQueryTextSubmit(final String query) {
    hideSuggestionList();
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
    hideSuggestionList();
    if (mSearchView != null && mIsSearchViewExpanded) {
      mSearchView.onActionViewCollapsed();
      mSearchView.setQuery("", false);
      mIsSearchViewExpanded = false;
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public void onGoogleSuggestResponse(List<String> response) {
    if (response != null && response.size() > 0) {
      ArrayAdapter<String> suggestionAdapter =
        new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, response);
      mSuggestionList.setAdapter(suggestionAdapter);
      mSuggestionList.setOnItemClickListener(this);
      mSuggestionList.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public void onGoogleSuggestFailure() {
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    TextView text = (TextView) view.findViewById(android.R.id.text1);
    if (text != null) {
      String query = (String) text.getText();
      if (!TextUtils.isEmpty(query)) {
        mSuggestionQuery = query;
        mSearchView.setQuery(query, true);
      }
    }
  }

  public void hideSuggestionList() {
    mSuggestionList.setVisibility(View.GONE);
  }
}
