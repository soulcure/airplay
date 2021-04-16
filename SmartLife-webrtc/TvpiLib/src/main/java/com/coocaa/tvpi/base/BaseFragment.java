package com.coocaa.tvpi.base;

import androidx.fragment.app.Fragment;

import com.coocaa.publib.views.LoadingDialog;

public class BaseFragment extends Fragment {

    protected LoadingDialog mNecessaryLoadingDlg;


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mNecessaryLoadingDlg != null) {
            mNecessaryLoadingDlg.dismiss();
            mNecessaryLoadingDlg = null;
        }
    }


    public void showLoading() {
        if (mNecessaryLoadingDlg == null) {
            mNecessaryLoadingDlg = new LoadingDialog(getContext());
            mNecessaryLoadingDlg.setCancelable(true);
            mNecessaryLoadingDlg.setCanceledOnTouchOutside(false);
        }

        if (mNecessaryLoadingDlg != null && !mNecessaryLoadingDlg.isShowing()) {
            mNecessaryLoadingDlg.show();
        }
    }

    public void dismissLoading() {
        if (mNecessaryLoadingDlg != null && mNecessaryLoadingDlg.isShowing()) {
            mNecessaryLoadingDlg.dismiss();
            mNecessaryLoadingDlg = null;
        }
    }
}
