package com.erbol.bo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.erbol.bo.Utils.AlertDialogUtil;
import com.erbol.bo.Utils.ConstantsUtil;
import com.erbol.bo.Utils.StateEthernet;

public class NewsActivity extends AppCompatActivity {
    private WebView wvNews;
    private ProgressBar progressBar;
    private static String urlWeb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        if (savedInstanceState != null) {
            urlWeb = savedInstanceState.getString(ConstantsUtil.WEBURL);
        } else {
            urlWeb = getIntent().getStringExtra(ConstantsUtil.WEBURL);
        }
        wvNews=(WebView)findViewById(R.id.wv_news);
        progressBar=(ProgressBar)findViewById(R.id.pb_news);
        if (StateEthernet.verificaConexion(this)){
            wvNews.setWebViewClient(new AppWebViewClients(progressBar));
            wvNews.getSettings().setJavaScriptEnabled(true);
            wvNews.loadUrl(urlWeb);
        }
        else{
            AlertDialogUtil.showAlertDialog(this);
        }
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onKeyDown(int paramInt, KeyEvent event) {
        if (event.getAction() == 0) {
            wvNews.canGoBack();
            finish();
        }
        return super.onKeyDown(paramInt, event);
    }
}