package com.erbol.bo.Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.erbol.bo.Adapters.OnListenerLine;
import com.erbol.bo.R;
import com.erbol.bo.Utils.AlertDialogUtil;
import com.erbol.bo.Utils.ConstantsUtil;
import com.erbol.bo.Utils.StateEthernet;

public class FDialog_conflict extends Dialog {
    private long idConf;
    private Activity mActivity;
    private OnListenerLine mListenerLine;
    private LinearLayout layout;

    private WebView wvConflict;
    private ProgressBar progressBar;
    private static String urlWeb;

    public FDialog_conflict(Activity mActivity, OnListenerLine mOnListenerL, long idConfr) {
        super(mActivity, R.style.DialogAnimationc);
        this.mActivity = mActivity;
        this.mListenerLine = mOnListenerL;
        this.idConf = idConfr;
        this.setContentView(getContentViews());
        this.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        this.setTitle(mActivity.getResources().getString(R.string.dialog_titlel));
        this.setCanceledOnTouchOutside(false);
    }

    public LinearLayout getContentViews() {
        urlWeb = ConstantsUtil.URL_DETAILC+idConf;
        final LayoutInflater inflater = LayoutInflater.from(mActivity);
        layout = (LinearLayout)inflater.inflate(R.layout.dialog_conflict, null);
        wvConflict=(WebView)layout.findViewById(R.id.wv_conflict);
        progressBar=(ProgressBar)layout.findViewById(R.id.pb_conflict);
        if (StateEthernet.verificaConexion(mActivity)){
            wvConflict.setWebViewClient(new AppWebViewClients(progressBar));
            wvConflict.getSettings().setJavaScriptEnabled(true);
            wvConflict.loadUrl(urlWeb);
        } else {
            AlertDialogUtil.showAlertDialog(mActivity);
        }
        return layout;
    }

    public class AppWebViewClients extends WebViewClient {
        private ProgressBar progressBar;

        public AppWebViewClients(ProgressBar progressBar) {
            this.progressBar=progressBar;
            progressBar.setVisibility(View.VISIBLE);
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onKeyDown(int paramInt, KeyEvent event) {
        if (event.getAction() == 0) {
            wvConflict.canGoBack();
            if(wvConflict != null) {
                wvConflict.loadUrl("about:blank");
                wvConflict.stopLoading();
                wvConflict = null;
            }
            mListenerLine.onListenerCanceled();
        }
        return super.onKeyDown(paramInt, event);
    }
}