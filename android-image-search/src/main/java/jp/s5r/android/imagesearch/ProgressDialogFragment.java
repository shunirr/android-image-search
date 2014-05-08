package jp.s5r.android.imagesearch;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

public class ProgressDialogFragment extends DialogFragment {

  public ProgressDialogFragment() {
    setCancelable(false);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    ProgressDialog dialog = new ProgressDialog(getActivity());
    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    dialog.setMessage("Loading...");
    return dialog;
  }
}
